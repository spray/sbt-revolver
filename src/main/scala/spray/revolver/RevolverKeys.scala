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

import sbt._

trait RevolverKeys {

  val reStart = InputKey[AppProcess]("re-start", "Starts the application in a forked JVM (in the background). " +
    "If it is already running the application is first stopped and then restarted.")

  val reStop = TaskKey[Unit]("re-stop", "Stops the application if it is currently running in the background")

  val reStatus = TaskKey[Unit]("re-status", "Shows information about the application that is potentially running")

  val reStartArgs = SettingKey[Seq[String]]("re-start-args",
    "The arguments to be passed to the applications main method when being started")

  val reForkOptions = TaskKey[ForkScalaRun]("re-fork-options", "The options needed for the start task for forking")

  val reJRebelJar = SettingKey[String]("re-jrebel-jar", "The path to the JRebel JAR. Automatically initialized to " +
    "value of the `JREBEL_PATH` environment variable.")

  val reColors = SettingKey[Seq[String]]("re-colors", "Colors used for tagging output from different processes")

  val reLogTag = SettingKey[String]("re-log-tag", "The tag used in front of log messages for this project")

  val debugSettings = SettingKey[Option[DebugSettings]]("debug-settings", "Settings for enabling remote JDWP debugging.")

}
