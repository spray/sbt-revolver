seq(lsSettings :_*)

sbtPlugin := true

version := "0.5.0"

name := "sbt-hot-deploy"

organization := "cc.spray"

homepage := Some(url("http://github.com/spray/sbt-hot-deploy"))

licenses in GlobalScope += "Apache License 2.0" -> url("https://github.com/spray/sbt-hot-deploy/raw/master/LICENSE")

(LsKeys.tags in LsKeys.lsync) := Seq("sbt-plugin", "sbt", "hotdeploy", "jrebel")

(LsKeys.docsUrl in LsKeys.lsync) <<= homepage

(description in LsKeys.lsync) :=
  "An sbt plugin which allows to run the project in the background while doing continuous recompilation."

