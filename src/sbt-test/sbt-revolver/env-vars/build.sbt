scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.7",
  "io.spray" %% "spray-can" % "1.3.2",
  "io.spray" %% "spray-routing" % "1.3.2"
)

enablePlugins(RevolverPlugin)

envVars in reStart += "TEST_VAR" -> "OK"
