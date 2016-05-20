package sbt.compat

object SbtCompat {
  implicit def convertProcess(process: sbt.Process): scala.sys.process.Process =
    new scala.sys.process.Process {
      def exitValue(): Int = process.exitValue()
      def destroy(): Unit = process.destroy()
    }
}