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

package cc.spray.revolver

import java.lang.{Runtime => JRuntime}
import sbt.Process

/**
 * A token which we put into the SBT state to hold the Process of an application running in the background.
 */
case class AppProcess(process: Process) {
  val shutdownHook = new Thread(new Runnable {
    def run() {
      println("Stopping the application that's still running in the background ...")
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