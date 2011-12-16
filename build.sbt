name := "sbt-revolver"

organization := "cc.spray"

version := "0.5.0"

description := "An SBT plugin for dangerously fast development turnaround"

homepage := Some(url("http://github.com/spray/sbt-revolver"))

licenses in GlobalScope += "Apache License 2.0" -> url("https://github.com/spray/sbt-revolver/raw/master/LICENSE")

sbtPlugin := true


///////////////
// publishing
///////////////

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishMavenStyle := true

publishTo <<= version { version =>
  Some {
    "snapshots" at {
      "http://nexus.scala-tools.org/content/repositories/" + {
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
