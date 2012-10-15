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
import scala.Console.{RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE}

object Utilities {

  def forkRun(config: ForkScalaRun, mainClass: String, classpath: Seq[File], options: Seq[String], log: Logger, extraJvmArgs: Seq[String]) = {
    log.info(options.mkString("Starting " + mainClass + ".main(", ", ", ")"))
    val scalaOptions = "-classpath" :: Path.makeString(classpath) :: mainClass :: options.toList
    val strategy = config.outputStrategy getOrElse LoggedOutput(log)
    Fork.scala.fork(config.javaHome, config.runJVMOptions ++ extraJvmArgs, config.scalaJars, scalaOptions, config.workingDirectory,
      config.connectInput, strategy)
  }

  def colorLogger(state: State): Logger = colorLogger(CommandSupport.logger(state))

  def colorLogger(logger: Logger): Logger = new Logger {
    def trace(t: => Throwable) { logger.trace(t) }
    def success(message: => String) { success(message) }
    def log(level: Level.Value, message: => String) {
      if (logger.ansiCodesSupported) {
        logger.log(level, message
          .replace("[RED]", RED)
          .replace("[GREEN]", GREEN)
          .replace("[YELLOW]", YELLOW)
          .replace("[BLUE]", BLUE)
          .replace("[MAGENTA]", MAGENTA)
          .replace("[CYAN]", CYAN)
          .replace("[WHITE]", WHITE))
      } else {
        logger.log(level, message
          .replace("[RED]", "")
          .replace("[GREEN]", "")
          .replace("[YELLOW]", "")
          .replace("[BLUE]", "")
          .replace("[MAGENTA]", "")
          .replace("[CYAN]", "")
          .replace("[WHITE]", ""))
      }
    }
  }

}