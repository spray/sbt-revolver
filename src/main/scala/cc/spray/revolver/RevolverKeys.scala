package cc.spray.revolver

import sbt._

trait RevolverKeys {

  val RE = config("re")

  val start = InputKey[AppProcess]("start", "Starts the application in a forked JVM (in the background). " +
    "If it is already running the application is first stopped and then restarted.") in RE

  val stop = TaskKey[Unit]("stop", "Stops the application if it is currently running in the background") in RE

  val status = TaskKey[Unit]("status", "Shows information about the application that is potentially running") in RE

  val startArgs = SettingKey[Seq[String]]("start-args",
    "The arguments to be passed to the applications main method when being started") in RE

  val forkOptions = TaskKey[ForkScalaRun]("fork-options", "The options needed for the start task for forking") in RE

  val jRebelJar = SettingKey[String]("jrebel-jar", "The path to the JRebel JAR. Automatically initialized to value " +
    "of the `JREBEL_PATH` environment variable.") in RE

}