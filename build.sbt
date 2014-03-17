name := "sbt-revolver"

organization := "io.spray"

version := "0.7.3-SNAPSHOT"

description := "An SBT plugin for dangerously fast development turnaround in Scala"

startYear := Some(2011)

homepage := Some(url("http://github.com/spray/sbt-revolver"))

organizationHomepage := Some(url("http://spray.io"))

licenses in GlobalScope += "Apache License 2.0" -> url("https://github.com/spray/sbt-revolver/raw/master/LICENSE")

sbtPlugin := true

scalacOptions := Seq("-deprecation", "-encoding", "utf8")

crossBuildingSettings

CrossBuilding.crossSbtVersions := Seq("0.11.3", "0.12", "0.13")

///////////////
// publishing
///////////////

publishMavenStyle := false

publishTo <<= (version) { version: String =>
   val base = new URL("http://scalasbt.artifactoryonline.com/scalasbt/")
   val name  = if (version.contains("-SNAPSHOT")) "snapshots" else "releases"
   Some(Resolver.url(name, new URL(base, "sbt-plugin-"+name))(Resolver.ivyStylePatterns))
}

credentials += Credentials(Path.userHome / ".ivy2" / "scalasbt.credentials")

///////////////
// ls-sbt
///////////////

seq(lsSettings :_*)

(LsKeys.tags in LsKeys.lsync) := Seq("sbt-plugin", "sbt", "plugin", "jrebel")

(LsKeys.docsUrl in LsKeys.lsync) <<= homepage
