package cc.spray

import java.lang.{ Runtime => JRuntime }

import sbt._
import sbt.Keys._
import Project.Initialize

object BackgroundService {
  import Keys._

  object Keys {
    val start = TaskKey[ServiceRun]("start",
      "Fork and run the current project in the background")
    val startArgs = SettingKey[Seq[String]]("start-args",
      "The arguments which should be given to the main method, when a service is started in the background with `start`")

    val stop = TaskKey[Unit]("stop",
      "Stops the task run in the background")

    val restart = TaskKey[ServiceRun]("restart",
      "Stops and restarts the current background service")

    val forkOptions = TaskKey[ForkScalaRun]("fork-options",
      "Collected options needed for forking to run a project")
  }

  lazy val backgroundServiceSettings =
    Seq(
      forkOptions <<= forkOptionsInit,
      startArgs := Seq.empty,
      start <<=
        (streams, state, forkOptions, mainClass in Compile, fullClasspath in Runtime, startArgs)
          .map(startService)
          .updateState(registerRun)
          .dependsOn(products in Compile),
      stop <<=
        (streams, state)
          .map(stopServiceWithStreams)
          .updateState(deregisterRun),

      // we need an extra task definition here and cannot simply dependOn stop and start
      // because a) dependencies may be executed in parallel and b) `updateState` transformations
      // are possibly run in the wrong order
      restart <<=
        (streams, state, forkOptions, mainClass in Compile, fullClasspath in Runtime, startArgs)
          .map(restartService)
          .updateState(registerRun)
          .dependsOn(products in Compile),

      // stop the service if the project is reloaded and the state is reset
      onUnload in Global ~= { onUnload =>
        state =>
          if (state.has(serviceRunKey))
            stopService(SysoutLogger, state)

          onUnload(state)
      }
    )

  def startService(streams: TaskStreams, state: State, option: ForkScalaRun, mainClass: Option[String], cp: Classpath, args: Seq[String]): ServiceRun = {
    if (state.has(serviceRunKey))
      throw new RuntimeException("Already started, please `stop` first or use `restart`")

    val runner = new BgForkRun(option)
    streams.log.info("Starting service...")
    val process =
      runner.run(mainClass.get, cp.map(_.data), args, SysoutLogger)

    ServiceRun(process)
  }

  def stopServiceWithStreams(streams: TaskStreams, state: State): Unit =
    stopService(streams.log, state)

  def stopService(log: Logger, state: State): Unit = {
    state.get(serviceRunKey) match {
      case Some(run) =>
        log.info("Aborting service.")
        run.stop()
      case None =>
        log.info("Service not yet started")
    }
  }

  def restartService(streams: TaskStreams, state: State, option: ForkScalaRun, mainClass: Option[String], cp: Classpath, args: Seq[String]): ServiceRun = {
    stopServiceWithStreams(streams, state)
    startService(streams, deregisterRun(state, ()), option, mainClass, cp, args)
  }

  /**
   * A token which we put into the sbt state, so that we can find it again
   * if we try to stop the service.
   */
  case class ServiceRun(process: Process) {
    val shutdownHook = new Thread(new Runnable {
      def run() {
        println("Stopping service")
        process.destroy()
      }
    })

    registerShutdownHook()

    def stop() {
      unregisterShutdownHook()
      process.destroy()
      process.exitValue()
    }

    def registerShutdownHook() {
      JRuntime.getRuntime.addShutdownHook(shutdownHook)
    }
    def unregisterShutdownHook() {
      JRuntime.getRuntime.removeShutdownHook(shutdownHook)
    }
  }
  val serviceRunKey = AttributeKey[ServiceRun]("service-run", "The current run")

  def registerRun(state: State, serviceRunValue: ServiceRun): State =
    state.put(serviceRunKey, serviceRunValue)
  def deregisterRun(state: State, dummy: Any): State =
    state.remove(serviceRunKey)

  /**
   * We got that directly from the sbt sources
   */
  def forkOptionsInit: Initialize[Task[ForkScalaRun]] =
    (taskTemporaryDirectory, scalaInstance, baseDirectory, javaOptions, outputStrategy, fork, javaHome, trapExit, connectInput) map {
				(tmp, si, base, options, strategy, forkRun, javaHomeDir, trap, connectIn) =>
      ForkOptions(scalaJars = si.jars, javaHome = javaHomeDir, connectInput = connectIn, outputStrategy = strategy,
					runJVMOptions = options, workingDirectory = Some(base))
		}
}
