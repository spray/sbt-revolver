package cc.spray

import sbt._

trait HotReloadKeys {

  val HR = config("hr")

  val start = InputKey[AppProcess]("start", "Starts the application in a forked JVM (in the background). " +
    "If it is already running the application first stopped and then restarted.") in HR

  val startArgs = SettingKey[Seq[String]]("start-args",
    "The arguments to be passed to the applications main method when being started") in HR

  // rather than `javaOptions in start`, we use this TaskKey (we need to initialize this key from a task)
  val startJavaOptions = TaskKey[Seq[String]]("start-java-options",
    "Java options to use for the `start` task when forking") in HR

  val stop = TaskKey[Unit]("stop", "Stops the application if it currently running in the background") in HR

  val forkOptions = TaskKey[ForkScalaRun]("fork-options", "The options needed for the start task for forking") in HR

  val jRebelJar = SettingKey[String]("jrebel-jar", "The path to the JRebel JAR. Automatically initialized to value " +
    "of the JREBEL_PATH environment variable.")

}