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

/**
 * A logger which logs directly with println to be used in situations where no streams are available
 */
class SysoutLogger(appName: String, color: String, ansiCodesSupported: Boolean = false) extends Logger {

  def trace(t: => Throwable) {
    t.printStackTrace()
    println(t)
  }

  def success(message: => String) {
    println(Utilities.colorize(ansiCodesSupported, "%s%s[RESET] success: " format (color, appName)) + message)
  }

  def log(level: Level.Value, message: => String) {
    val levelStr = level match {
      case Level.Info => ""
      case Level.Error => "[ERROR]"
      case x@_ => x.toString
    }
    println(Utilities.colorize(ansiCodesSupported, "%s%s[RESET]%s " format (color, appName, levelStr)) + message)
  }
}

object SysoutLogger extends SysoutLogger("app", "[BOLD]", false)
