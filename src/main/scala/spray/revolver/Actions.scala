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
import sbt._
import java.io.File

object Actions {
  import Utilities._

  val revolverStateKey = AttributeKey[RevolverState]("revolver-state", "Global state of sbt-revolver")

  def updateState(state: State)(f: RevolverState => RevolverState): State =
    state.put(revolverStateKey, f(revolverState(state)))
  def updateStateAndGet[T](state: State)(f: RevolverState => (T, RevolverState)): (T, State) = {
    val (value, newState) = f(revolverState(state))
    (value, state.put(revolverStateKey, newState))
  }

  def revolverState(state: State): RevolverState = state.get(revolverStateKey).getOrElse(RevolverState.initial)

  def registerAppProcess(state: State, info: (ProjectRef, AppProcess)) = info match {
    case (project, process) => updateState(state)(_.addProcess(project, process))
  }

  def unregisterAppProcess(state: State, project: (ProjectRef, Any)) =
    updateState(state)(_.removeProcess(project._1))

  def restartApp(streams: TaskStreams, state: State, project: ProjectRef, option: ForkScalaRun, mainClass: Option[String],
                 cp: Classpath, args: Seq[String], startConfig: ExtraCmdLineOptions): (ProjectRef, AppProcess) = {
    stopAppWithStreams(streams, state, project)
    project -> startApp(streams, unregisterAppProcess(state, (project, ())), project, option, mainClass, cp, args, startConfig)
  }

  def startApp(streams: TaskStreams, state: State, project: ProjectRef, options: ForkScalaRun, mainClass: Option[String],
               cp: Classpath, args: Seq[String], startConfig: ExtraCmdLineOptions): AppProcess = {
    assert(!revolverState(state).getProcess(project).exists(_.isRunning))

    val color = Utilities.nextColor()
    val logger = new SysoutLogger(project.project, color, streams.log.ansiCodesSupported)
    colorLogger(streams.log).info("[YELLOW]Starting application %s in the background ..." format formatAppName(project.project, color))
    AppProcess(project.project, color, logger) {
      forkRun(options, mainClass.get, cp.map(_.data), args ++ startConfig.startArgs, logger, startConfig.jvmArgs)
    }
  }

  def stopAppWithStreams(streams: TaskStreams, state: State, project: ProjectRef) = {
    project -> stopApp(colorLogger(streams.log), state, project)
  }

  def stopApp(log: Logger, state: State, project: ProjectRef) {
    revolverState(state).getProcess(project) match {
      case Some(appProcess) =>
        if (appProcess.isRunning) {
          log.info("[YELLOW]Stopping application %s (by killing the forked JVM) ..." format formatApp(appProcess))

          appProcess.stop()
        }
      case None =>
        log.info("[YELLOW]Application %s not yet started" format formatAppName(project.project, "[BOLD]"))
    }
  }

  def stopApps(log: Logger, state: State): Unit =
    revolverState(state).runningProjects.foreach(stopApp(log, state, _))

  def showStatus(streams: TaskStreams, state: State, project: ProjectRef): Unit =
    colorLogger(streams.log).info {
      revolverState(state).getProcess(project).find(_.isRunning) match {
        case Some(appProcess) =>
          "[GREEN]Application %s is currently running" format formatApp(appProcess, color = "[GREEN]")
        case None =>
          "[YELLOW]Application %s is currently NOT running" format formatAppName(project.project, "[BOLD]")
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

  case class ExtraCmdLineOptions(jvmArgs: Seq[String], startArgs: Seq[String])

  import complete.Parsers._
  import complete.Parser._
  val spaceDelimitedWithoutDashes =
          (token(Space) ~> (token(NotSpace, "<args>") - "---")).* <~ SpaceClass.*
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
}
