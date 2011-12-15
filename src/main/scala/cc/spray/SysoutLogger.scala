package cc.spray

import sbt._

/**
 * A logger which logs directly with println to be used in situations where no streams are
 * available
 */
object SysoutLogger extends Logger {

  def trace(t: => Throwable) {
    t.printStackTrace()
    println(t)
  }

  def success(message: => String) {
    println("success: " + message)
  }

  def log(level: Level.Value, message: => String) {
    val levelStr = level match {
      case Level.Info => "service"
      case Level.Error => "service[ERROR]"
      case x@_ => x.toString
    }
    println(levelStr + ": " + message)
  }
}
