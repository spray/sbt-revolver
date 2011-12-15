name := "sbt-hot-reload"

organization := "cc.spray"

version := "0.5.0"

description := "An sbt plugin which allows to run the project in the background while doing continuous recompilation."

homepage := Some(url("http://github.com/spray/sbt-hot-reload"))

licenses in GlobalScope += "Apache License 2.0" -> url("https://github.com/spray/sbt-hot-reload/raw/master/LICENSE")

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

(LsKeys.tags in LsKeys.lsync) := Seq("sbt-plugin", "sbt", "jrebel")

(LsKeys.docsUrl in LsKeys.lsync) <<= homepage