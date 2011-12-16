package cc.spray.revolver

import sbt.Keys._
import sbt._
import java.io.File

object Actions {
  import Utilities._

  val appProcessKey = AttributeKey[AppProcess]("app-process", "The currently running application process")

  def registerAppProcess(state: State, appProcess: AppProcess) =
    state.put(appProcessKey, appProcess)

  def unregisterAppProcess(state: State, dummy: Any) =
    state.remove(appProcessKey)

  def restartApp(streams: TaskStreams, state: State, option: ForkScalaRun, mainClass: Option[String],
                 cp: Classpath, args: Seq[String], extraArgs: Seq[String]): AppProcess = {
    stopAppWithStreams(streams, state)
    startApp(streams, unregisterAppProcess(state, ()), option, mainClass, cp, args, extraArgs)
  }

  def startApp(streams: TaskStreams, state: State, options: ForkScalaRun, mainClass: Option[String],
               cp: Classpath, args: Seq[String], extraArgs: Seq[String]): AppProcess = {
    assert(!state.has(appProcessKey))
    colorLogger(streams.log).info("[YELLOW]Starting application in the background ...")
    AppProcess {
      forkRun(options, mainClass.get, cp.map(_.data), args ++ extraArgs, SysoutLogger)
    }
  }

  def stopAppWithStreams(streams: TaskStreams, state: State) {
    stopApp(colorLogger(streams.log), state)
  }

  def stopApp(log: Logger, state: State) {
    state.get(appProcessKey) match {
      case Some(appProcess) =>
        log.info("[YELLOW]Stopping application (by killing the forked JVM) ...")
        appProcess.stop()
      case None =>
        log.info("[YELLOW]Application not yet started")
    }
  }

  def showStatus(streams: TaskStreams, state: State) {
    colorLogger(streams.log).info {
      if (state.get(appProcessKey).isDefined) "[GREEN]Application is currently running"
      else "[YELLOW]Application is currently NOT running"
    }
  }

  def createJRebelAgentOption(log: Logger, path: String): Option[String] = {
    if (!path.trim.isEmpty) {
      val file = new File(path)
      if (!file.exists || !file.isFile) {
        val file2 = new File(file, "jrebel.jar")
        if (!file2.exists || !file2.isFile) {
          log.warn("jrebel.jar: " + path + " not found")
          None
        } else Some("-javaagent:" + file2.getAbsolutePath)
      } else Some("-javaagent:" + path)
    } else None
  }
}