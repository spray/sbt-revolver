package cc.spray

import java.lang.{ Runtime => JRuntime }

import sbt._
import sbt.Keys._
import Project.Initialize

object HRPlugin extends Plugin {
  import HotReload._

  object HotReload {
    import Impl._

    val HR = config("hr")

    val start = InputKey[ServiceRun]("start",
      "Fork and run the current project in the background. If it was started before it is first stopped") in HR
    val startArgs = SettingKey[Seq[String]]("start-args",
      "The arguments which should be given to the main method, when a service is started in the background with `start`") in HR

    // we very much would like to use `javaOptions in start` here,
    // but that's not possible because javaOptions is a SettingKey
    // and we have to depend on a task
    val startJavaOptions = TaskKey[Seq[String]]("start-java-options",
      "Java options to use for the start task when forking") in HR

    val stop = TaskKey[Unit]("stop",
      "Stops the task run in the background") in HR

    val forkOptions = TaskKey[ForkScalaRun]("fork-options",
      "Collected options needed for the start task for forking") in HR

    val jRebelJar = SettingKey[String]("jrebel-jar",
      "The path to the JRebel jar")

    lazy val hotReloadSettings =
      Seq(
        forkOptions <<= forkOptionsInit,
        startArgs in Global := Seq.empty,
        start <<= inputTask { args =>
          (streams, state, forkOptions, mainClass in Compile, fullClasspath in Runtime, startArgs, args)
            .map(restartService)
            .updateState(registerRun)
            .dependsOn(products in Compile)
        },
        stop <<=
          (streams, state)
            .map(stopServiceWithStreams)
            .updateState(deregisterRun),

        // stop the service if the project is reloaded and the state is reset
        onUnload in Global ~= { onUnload =>
          state =>
            if (state.has(serviceRunKey))
              stopService(SysoutLogger, state)

            onUnload(state)
        },

        jRebelJar in Global := Option(System.getenv("JREBEL_PATH")).getOrElse(""),

        startJavaOptions <<= (javaOptions, streams, jRebelJar) map { (options, streams, jRebelJar) =>
          options ++ createJRebelAgentOption(streams.log, jRebelJar).toSeq
        }
      )
  }

  object Impl {
    def startService(streams: TaskStreams, state: State, option: ForkScalaRun, mainClass: Option[String],
                     cp: Classpath, args: Seq[String], extraArgs: Seq[String]): ServiceRun = {
      assert(!state.has(serviceRunKey))
      val runner = new BgForkRun(option)
      streams.log.info("Starting service...")
      val process = runner.run(mainClass.get, cp.map(_.data), args ++ extraArgs, SysoutLogger)
      ServiceRun(process)
    }

    def stopServiceWithStreams(streams: TaskStreams, state: State) {
      stopService(streams.log, state)
    }

    def stopService(log: Logger, state: State) {
      state.get(serviceRunKey) match {
        case Some(run) =>
          log.info("Aborting service.")
          run.stop()
        case None =>
          log.info("Service not yet started")
      }
    }

    def restartService(streams: TaskStreams, state: State, option: ForkScalaRun, mainClass: Option[String],
                       cp: Classpath, args: Seq[String], extraArgs: Seq[String]): ServiceRun = {
      stopServiceWithStreams(streams, state)
      startService(streams, deregisterRun(state, ()), option, mainClass, cp, args, extraArgs)
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
    def forkOptionsInit: Initialize[Task[ForkScalaRun]] = {
      (taskTemporaryDirectory, scalaInstance, baseDirectory, startJavaOptions, outputStrategy, javaHome) map {
        (tmp, si, base, options, strategy, javaHomeDir) => {
          ForkOptions(
            scalaJars = si.jars,
            javaHome = javaHomeDir,
            connectInput = false,
            outputStrategy = strategy,
            runJVMOptions = options,
            workingDirectory = Some(base)
          )
        }
      }
    }

    def createJRebelAgentOption(log: Logger, path: String): Option[String] =
      if (path.trim.isEmpty)
        None
      else {
        val jRebelJarFile = file(path)
        if (jRebelJarFile.exists && jRebelJarFile.isFile)
          Some("-javaagent:"+path)
        else if (new File(jRebelJarFile, "jrebel.jar").exists)
          Some("-javaagent:"+new File(jRebelJarFile, "jrebel.jar").getAbsolutePath)
        else {
          log.warn("At '"+path+"' no jrebel.jar was found")
          None
        }
      }
  }
}
