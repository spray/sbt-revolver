package scala.sys.process

import java.lang.{Process => JProcess}
import sbt.{Level, Logger}
import scala.util.control.NonFatal

case class ProcessWithPid(process: Process, pid: Option[Long])

object ProcessWithPid {
  private def reflectJProcess(p: Process.SimpleProcess): JProcess = {
    val field = p.getClass.getDeclaredField("p")
    field.setAccessible(true)
    field.get(p).asInstanceOf[JProcess]
  }

  // Java 9+ has a `Process#pid()` method, but Java 8 and below have a private `Process#pid` field
  // We first try to reflect on the method and then fall back to reflecting on the field
  private def reflectJProcessPid(p: JProcess): Long =
    try {
      val method = classOf[JProcess].getMethod("pid")
      method.invoke(p) match {
        case pid: java.lang.Long => pid
        case pid => throw new RuntimeException(s"Expected process PID ($pid) to be a Long, but it was a ${pid.getClass.getName}")
      }
    } catch {
      case e: NoSuchMethodException =>
        val field = p.getClass.getDeclaredField("pid")
        field.setAccessible(true)
        field.getLong(p)
    }

  def apply(process: Process, log: Logger): ProcessWithPid =
    try {
      process match {
        case p: Process.SimpleProcess =>
          val jp = reflectJProcess(p)
          val pid = reflectJProcessPid(jp)
          ProcessWithPid(process, Some(pid))

        case p =>
          throw new RuntimeException(s"Expected app process to be a Process.SimpleProcess but it was a ${p.getClass.getName}")
      }
    } catch {
      case NonFatal(e) =>
        log.log(Level.Warn, s"Failed to determine process PID: $e")
        ProcessWithPid(process, None)
    }
}
