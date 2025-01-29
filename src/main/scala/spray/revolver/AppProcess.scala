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

import java.lang.{Runtime => JRuntime}
import java.util.concurrent.TimeUnit
import sbt.{Logger, ProjectRef}

import scala.sys.process.ProcessWithPid

/**
 * A token which we put into the SBT state to hold the Process of an application running in the background.
 */
case class AppProcess(projectRef: ProjectRef, consoleColor: String, log: Logger)(process: ProcessWithPid) {
  val shutdownHook = createShutdownHook("... killing ...")

  private def destroyProcess(): Unit = process.process.destroy()

  private def killProcess(pid: Long): Unit = {
    val exited = try {
      JRuntime.getRuntime.exec(s"kill -15 $pid").waitFor(10, TimeUnit.SECONDS)
    } catch { case e: InterruptedException => true }

    if (!exited) destroyProcess()
  }

  private def stopProcess(): Int = {
    process.pid.fold(destroyProcess())(killProcess)
    process.process.exitValue()
  }

  def createShutdownHook(msg: => String) =
    new Thread(new Runnable {
      def run() {
        if (isRunning) {
          log.info(msg)
          stopProcess()
        }
      }
    })

  @volatile var finishState: Option[Int] = None

  val watchThread = {
    val thread = new Thread(new Runnable {
      def run() {
        val code = process.process.exitValue()
        finishState = Some(code)
        log.info("... finished with exit code %d" format code)
        unregisterShutdownHook()
        Actions.unregisterAppProcess(projectRef)
      }
    })
    thread.start()
    thread
  }
  def projectName: String = projectRef.project

  registerShutdownHook()

  def stop() {
    unregisterShutdownHook()
    stopProcess()
  }

  def registerShutdownHook() {
    JRuntime.getRuntime.addShutdownHook(shutdownHook)
  }

  def unregisterShutdownHook() {
    JRuntime.getRuntime.removeShutdownHook(shutdownHook)
  }

  def isRunning: Boolean =
    finishState.isEmpty
}
