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
import scala.Console._

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
    def log(level: Level.Value, message: => String): Unit =
      logger.log(level, colorize(logger.ansiCodesSupported, message))
  }

  val simpleColors =
    Seq(
      "RED" -> RED,
      "GREEN" -> GREEN,
      "YELLOW" -> YELLOW,
      "BLUE" -> BLUE,
      "MAGENTA" -> MAGENTA,
      "CYAN" -> CYAN,
      "WHITE" -> WHITE
    )
  val rgbColors = (0 to 255) map rgb
  val ansiTagMapping: Seq[(String, String)] =
    (
      Seq(
        "BOLD" -> BOLD,
        "RESET" -> RESET
      ) ++
      simpleColors ++
      simpleColors.map(reversed) ++
      simpleColors.map(underlined) ++
      rgbColors
    ).map(delimited("[", "]"))

  def reversed(color: (String, String)): (String, String) =
    ("~"+color._1) -> (color._2+REVERSED)
  def underlined(color: (String, String)): (String, String) =
    ("_"+color._1) -> (color._2+UNDERLINED)
  def delimited(before: String, after: String)(mapping: (String, String)): (String, String) =
    (before+mapping._1+after, mapping._2)
  def rgb(idx: Int): (String, String) = ("RGB"+idx, "\033[38;5;"+idx+"m")

  def replaceAll(message: String, replacer: String => String) =
    ansiTagMapping.foldLeft(message)((msg, tag) => msg.replaceAll(java.util.regex.Pattern.quote(tag._1), replacer(tag._2)))

  def colorize(ansiCodesSupported: Boolean, message: String): String =
    replaceAll(message, if (ansiCodesSupported) identity else _ => "")
}
