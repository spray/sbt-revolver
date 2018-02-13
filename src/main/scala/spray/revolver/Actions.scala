/*
 * Copyright (C) 2009-2012 Johannes Rudolph and Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spray.revolver

import sbt.Keys._
import sbt.{Fork, ForkOptions, LoggedOutput, Logger, Path, ProjectRef, State, complete}
import java.io.File
import SbtCompat._

object Actions {
  import Utilities._

  def restartApp(streams: TaskStreams, logTag: String, project: ProjectRef, option: ForkOptions, mainClass: Option[String],
                 cp: Classpath, args: Seq[String], startConfig: ExtraCmdLineOptions): AppProcess = {
    stopAppWithStreams(streams, project)
    startApp(streams, logTag, project, option, mainClass, cp, args, startConfig)
  }

  def startApp(streams: TaskStreams, logTag: String, project: ProjectRef, options: ForkOptions, mainClass: Option[String],
               cp: Classpath, args: Seq[String], startConfig: ExtraCmdLineOptions): AppProcess = {
    assert(!revolverState.getProcess(project).exists(_.isRunning))

    // fail early
    val theMainClass = mainClass.getOrElse(sys.error("No main class detected!"))
    val color = updateStateAndGet(_.takeColor)
    val logger = new SysoutLogger(logTag, color, streams.log.ansiCodesSupported)
    colorLogger(streams.log).info("[YELLOW]Starting application %s in the background ..." format formatAppName(project.project, color))

    val appProcess=
      AppProcess(project, color, logger) {
        forkRun(options, theMainClass, cp.map(_.data), args ++ startConfig.startArgs, logger, startConfig.jvmArgs)
      }
    registerAppProcess(project, appProcess)
    appProcess
  }

  def stopAppWithStreams(streams: TaskStreams, project: ProjectRef) = stopApp(colorLogger(streams.log), project)

  def stopApp(log: Logger, project: ProjectRef): Unit = {
    revolverState.getProcess(project) match {
      case Some(appProcess) =>
        if (appProcess.isRunning) {
          log.info("[YELLOW]Stopping application %s (by killing the forked JVM) ..." format formatApp(appProcess))

          appProcess.stop()
        }
      case None =>
        log.info("[YELLOW]Application %s not yet started" format formatAppName(project.project, "[BOLD]"))
    }
    unregisterAppProcess(project)
  }

  def stopApps(log: Logger): Unit =
    revolverState.runningProjects.foreach(stopApp(log, _))

  def showStatus(streams: TaskStreams, project: ProjectRef): Unit =
    colorLogger(streams.log).info {
      revolverState.getProcess(project).find(_.isRunning) match {
        case Some(appProcess) =>
          "[GREEN]Application %s is currently running" format formatApp(appProcess, color = "[GREEN]")
        case None =>
          "[YELLOW]Application %s is currently NOT running" format formatAppName(project.project, "[BOLD]")
      }
    }

  def createJRebelAgentOption(log: Logger, jarPath: String, agentPath: String): Option[String] = {

    def checkPath(path: String, vmOption: String, checkAgentpath: Boolean): Option[String] = {
      val file = new File(path)
      if (!file.exists || !file.isFile) {
        if (checkAgentpath) {
          log.warn("jRebel agent: " + path + " not found")
          None
        } else {
          val file2 = new File(path, "jrebel.jar")
          if (!file2.exists || !file2.isFile) {
            log.warn("jrebel.jar: " + path + " not found")
            None
          } else {
            Some(vmOption + file2.getAbsolutePath)
          }
        }
      } else {
        Some(vmOption + file.getAbsolutePath)
      }
    }

    if (!agentPath.trim.isEmpty) {
      checkPath(agentPath.trim, "-agentpath:", true)
    } else if (!jarPath.trim.isEmpty) {
      checkPath(jarPath.trim, "-javaagent:", false)
    } else None
  }

  def updateState(f: RevolverState => RevolverState): Unit = GlobalState.update(f)
  def updateStateAndGet[T](f: RevolverState => (RevolverState, T)): T = GlobalState.updateAndGet(f)
  def revolverState: RevolverState = GlobalState.get()

  def registerAppProcess(project: ProjectRef, process: AppProcess) =
    updateState { state =>
      // before we overwrite the process entry we have to make sure the old
      // project is really closed to avoid the unlikely (impossible?) race condition that we
      // have started two processes concurrently but only register the second one
      val oldProcess = state.getProcess(project)
      if (oldProcess.exists(_.isRunning)) oldProcess.get.stop()

      state.addProcess(project, process)
    }

  def unregisterAppProcess(project: ProjectRef) = updateState(_.removeProcessAndColor(project))

  case class ExtraCmdLineOptions(jvmArgs: Seq[String], startArgs: Seq[String])

  import complete.Parsers._
  import complete.Parser._
  val spaceDelimitedWithoutDashes =
          (token(Space) ~> and(token(NotSpace, "<args>"), not("---", "Excluded."))).* <~ SpaceClass.*
  /*
   * A parser which parses additional options to the start task of the form
   * <arg1> <arg2> ... <argN> --- <jvmArg1> <jvmArg2> ... <jvmArgN>
   */
  val startArgsParser: State => complete.Parser[ExtraCmdLineOptions] = { (state: State) =>
    (spaceDelimitedWithoutDashes ~ (SpaceClass.* ~ "---" ~ SpaceClass.* ~> spaceDelimited("<jvm-args>")).?) map {
      case (a, b) =>
        ExtraCmdLineOptions(b.getOrElse(Nil), a)
    }
  }

  def formatApp(process: AppProcess, color: String = "[YELLOW]"): String =
    formatAppName(process.projectName, process.consoleColor, color)
  def formatAppName(projectName: String, projectColor: String, color: String = "[YELLOW]"): String =
    "[RESET]%s%s[RESET]%s" format (projectColor, projectName, color)

  def forkRun(config: ForkOptions, mainClass: String, classpath: Seq[File], options: Seq[String], log: Logger, extraJvmArgs: Seq[String]): Process = {
    log.info(options.mkString("Starting " + mainClass + ".main(", ", ", ")"))
    val scalaOptions = "-classpath" :: Path.makeString(classpath) :: mainClass :: options.toList
    val newOptions = config
      .withOutputStrategy(config.outputStrategy getOrElse LoggedOutput(log))
      .withRunJVMOptions(config.runJVMOptions ++ extraJvmArgs)

    Fork.java.fork(newOptions, scalaOptions)
  }
}
