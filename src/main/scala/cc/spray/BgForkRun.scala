package cc.spray

import sbt._

/**
 * This is mainly copied from the sbt sources but adapted to return the started process
 */
class BgForkRun(config: ForkScalaRun) {

	def run(mainClass: String, classpath: Seq[File], options: Seq[String], log: Logger): Process = {
		log.info("Running " + mainClass + " " + options.mkString(" "))

		val scalaOptions = classpathOption(classpath) ::: mainClass :: options.toList
		val strategy = config.outputStrategy getOrElse LoggedOutput(log)
		Fork.scala.fork(config.javaHome, config.runJVMOptions, config.scalaJars, scalaOptions, config.workingDirectory,
      config.connectInput, strategy)
	}

	private def classpathOption(classpath: Seq[File]) = "-classpath" :: Path.makeString(classpath) :: Nil

}
