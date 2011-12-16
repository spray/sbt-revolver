package cc.spray.revolver

import sbt._

trait RevolverKeys {

  val RE = config("re")

  val start = InputKey[AppProcess]("start", "Starts the application in a forked JVM (in the background). " +
    "If it is already running the application is first stopped and then restarted.") in RE

  val startArgs = SettingKey[Seq[String]]("start-args",
    "The arguments to be passed to the applications main method when being started") in RE

  // rather than `javaOptions in start`, we use this TaskKey (we need to initialize this key from a task)
  val startJavaOptions = TaskKey[Seq[String]]("start-java-options",
    "Java options to use for the `start` task when forking") in RE

  val stop = TaskKey[Unit]("stop", "Stops the application if it is currently running in the background") in RE

  val forkOptions = TaskKey[ForkScalaRun]("fork-options", "The options needed for the start task for forking") in RE

  val jRebelJar = SettingKey[String]("jrebel-jar", "The path to the JRebel JAR. Automatically initialized to value " +
    "of the `JREBEL_PATH` environment variable.")

}