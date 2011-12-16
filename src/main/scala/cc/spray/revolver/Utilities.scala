package cc.spray.revolver

import sbt._
import scala.Console.{RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE}

object Utilities {

  def forkRun(config: ForkScalaRun, mainClass: String, classpath: Seq[File], options: Seq[String], log: Logger) = {
    log.info(options.mkString("Starting " + mainClass + ".main(", ", ", ")"))
    val scalaOptions = "-classpath" :: Path.makeString(classpath) :: mainClass :: options.toList
    val strategy = config.outputStrategy getOrElse LoggedOutput(log)
    Fork.scala.fork(config.javaHome, config.runJVMOptions, config.scalaJars, scalaOptions, config.workingDirectory,
      config.connectInput, strategy)
  }

  def colorLogger(state: State): Logger = colorLogger(CommandSupport.logger(state))

  def colorLogger(logger: Logger): Logger = new Logger {
    def trace(t: => Throwable) { logger.trace(t) }
    def success(message: => String) { success(message) }
    def log(level: Level.Value, message: => String) {
      if (logger.ansiCodesSupported) {
        logger.log(level, message
          .replace("[RED]", RED)
          .replace("[GREEN]", GREEN)
          .replace("[YELLOW]", YELLOW)
          .replace("[BLUE]", BLUE)
          .replace("[MAGENTA]", MAGENTA)
          .replace("[CYAN]", CYAN)
          .replace("[WHITE]", WHITE))
      } else {
        logger.log(level, message
          .replace("[RED]", "")
          .replace("[GREEN]", "")
          .replace("[YELLOW]", "")
          .replace("[BLUE]", "")
          .replace("[MAGENTA]", "")
          .replace("[CYAN]", "")
          .replace("[WHITE]", ""))
      }
    }
  }

}