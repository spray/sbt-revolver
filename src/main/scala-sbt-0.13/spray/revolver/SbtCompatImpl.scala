package spray.revolver

import sbt._
import Keys._

object SbtCompatImpl extends SbtCompat with RevolverKeys {
  def changeJavaOptionsWithExtra[T](extra: SettingKey[T])(f: (Seq[String], String, T) => Seq[String]): Setting[_] =
    javaOptions in reStart <<= (javaOptions, reJRebelJar, extra) map f

  def forkRun(config: ForkOptions, mainClass: String, classpath: Seq[File], options: Seq[String], log: Logger, extraJvmArgs: Seq[String]): Process = {
    log.info(options.mkString("Starting " + mainClass + ".main(", ", ", ")"))
    val scalaOptions = "-classpath" :: Path.makeString(classpath) :: mainClass :: options.toList
    val newOptions =
      config.copy(
        outputStrategy = Some(config.outputStrategy getOrElse LoggedOutput(log)),
        runJVMOptions = config.runJVMOptions ++ extraJvmArgs)

    Fork.java.fork(newOptions, scalaOptions)
  }
}
