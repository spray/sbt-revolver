name := "sbt-revolver"

organization := "io.spray"

version := "0.10.0-SNAPSHOT"

description := "An SBT plugin for dangerously fast development turnaround in Scala"

startYear := Some(2011)

homepage := Some(url("http://github.com/spray/sbt-revolver"))

organizationHomepage := Some(url("http://spray.io"))

licenses in GlobalScope += "Apache License 2.0" -> url("https://github.com/spray/sbt-revolver/raw/master/LICENSE")

publishMavenStyle := false

bintrayRepository := "sbt-plugins"

bintrayOrganization := None
