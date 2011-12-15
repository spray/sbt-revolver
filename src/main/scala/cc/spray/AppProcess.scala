package cc.spray

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