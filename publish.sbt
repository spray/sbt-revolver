name := "sbt-revolver"

organization := "io.spray"

description := "An SBT plugin for dangerously fast development turnaround in Scala"

startYear := Some(2011)

homepage := Some(url("http://github.com/spray/sbt-revolver"))

organizationHomepage := Some(url("http://spray.io"))

licenses += "Apache License 2.0" -> url("https://github.com/spray/sbt-revolver/raw/master/LICENSE")

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  Some {
    if (version.value.trim.contains("+")) "snapshots" at nexus + "content/repositories/snapshots"
    else                                  "releases"  at nexus + "service/local/staging/deploy/maven2"
  }
}

publishMavenStyle := true
Test / publishArtifact := false
pomIncludeRepository := { _ => false }

scmInfo := Some(
  ScmInfo(
    browseUrl = url("https://github.com/spray/sbt-revolver"),
    connection = "scm:git:git@github.com:spray/sbt-revolver.git"
  )
)

developers := List(
  Developer(
    "sbt-revolver-contributors",
    "Sbt Revolver Contributors",
    "",
    url("https://github.com/spray/sbt-revolver/graphs/contributors"))
)
