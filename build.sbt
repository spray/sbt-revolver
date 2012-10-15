name := "sbt-revolver"

organization := "io.spray"

version := "0.6.2-SNAPSHOT"

description := "An SBT plugin for dangerously fast development turnaround in Scala"

startYear := Some(2011)

homepage := Some(url("http://github.com/spray/sbt-revolver"))

organizationHomepage := Some(url("http://spray.io"))

licenses in GlobalScope += "Apache License 2.0" -> url("https://github.com/spray/sbt-revolver/raw/master/LICENSE")

sbtPlugin := true

scalacOptions := Seq("-deprecation", "-encoding", "utf8")

CrossBuilding.crossSbtVersions := Seq("0.11.3", "0.12")

///////////////
// publishing
///////////////

publishMavenStyle := false

publishTo := Some(Resolver.url("sbt-plugin-releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns))

///////////////
// ls-sbt
///////////////

seq(lsSettings :_*)

(LsKeys.tags in LsKeys.lsync) := Seq("sbt-plugin", "sbt", "plugin", "jrebel")

(LsKeys.docsUrl in LsKeys.lsync) <<= homepage
