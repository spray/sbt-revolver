name := "sbt-revolver"

organization := "cc.spray"

version := "0.6.1"

description := "An SBT plugin for dangerously fast development turnaround in Scala"

startYear := Some(2011)

homepage := Some(url("http://github.com/spray/sbt-revolver"))

organizationHomepage := Some(url("http://spray.cc"))

licenses in GlobalScope += "Apache License 2.0" -> url("https://github.com/spray/sbt-revolver/raw/master/LICENSE")

sbtPlugin := true

scalaVersion := "2.9.2"

scalacOptions := Seq("-deprecation", "-encoding", "utf8")


///////////////
// publishing
///////////////

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishMavenStyle := true

publishTo <<= version { version =>
  Some {
    "spray nexus" at {
      // public uri is repo.spray.cc, we use an SSH tunnel to the nexus here
      "http://localhost:42424/content/repositories/" + {
        if (version.trim.endsWith("SNAPSHOT")) "snapshots/" else"releases/"
      }
    }
  }
}


///////////////
// ls-sbt
///////////////

seq(lsSettings :_*)

(LsKeys.tags in LsKeys.lsync) := Seq("sbt-plugin", "sbt", "plugin", "jrebel")

(LsKeys.docsUrl in LsKeys.lsync) <<= homepage
