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

  val appProcessKey = AttributeKey[AppProcess]("app-process", "The currently running application process")

  def registerAppProcess(state: State, appProcess: AppProcess) =
    state.put(appProcessKey, appProcess)

  def unregisterAppProcess(state: State, dummy: Any) =
    state.remove(appProcessKey)

  def restartApp(streams: TaskStreams, state: State, option: ForkScalaRun, mainClass: Option[String],
                 cp: Classpath, args: Seq[String], startConfig: ExtraCmdLineOptions): AppProcess = {
    stopAppWithStreams(streams, state)
    startApp(streams, unregisterAppProcess(state, ()), option, mainClass, cp, args, startConfig)
  }

  def startApp(streams: TaskStreams, state: State, options: ForkScalaRun, mainClass: Option[String],
               cp: Classpath, args: Seq[String], startConfig: ExtraCmdLineOptions): AppProcess = {
    assert(!state.has(appProcessKey))
    colorLogger(streams.log).info("[YELLOW]Starting application in the background ...")
    AppProcess {
      forkRun(options, mainClass.get, cp.map(_.data), args ++ startConfig.startArgs, SysoutLogger, startConfig.jvmArgs)
    }
  }

  def stopAppWithStreams(streams: TaskStreams, state: State) {
    stopApp(colorLogger(streams.log), state)
  }

  def stopApp(log: Logger, state: State) {
    state.get(appProcessKey) match {
      case Some(appProcess) =>
        if (appProcess.isRunning) {
          log.info("[YELLOW]Stopping application (by killing the forked JVM) ...")

          appProcess.stop()
        }
      case None =>
        log.info("[YELLOW]Application not yet started")
    }
  }

  def showStatus(streams: TaskStreams, state: State) {
    colorLogger(streams.log).info {
      if (state.get(appProcessKey).exists(_.isRunning)) "[GREEN]Application is currently running"
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
}