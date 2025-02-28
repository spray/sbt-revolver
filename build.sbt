lazy val scala212 = "2.12.20"
lazy val scala3 = "3.6.3"

scalaVersion := scala212
crossScalaVersions := Seq(scala212, scala3)

enablePlugins(SbtPlugin)

pluginCrossBuild / sbtVersion := {
  scalaBinaryVersion.value match {
    case "2.12" => "1.8.2"
    case _ => "2.0.0-M3"
  }
}

scalacOptions := Seq("-deprecation", "-encoding", "utf8")

scriptedLaunchOpts += s"-Dplugin.version=${version.value}"
scriptedBufferLog := false
Test / test := (Test / test).dependsOn(scripted.toTask("")).value
