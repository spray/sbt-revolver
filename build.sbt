import sbt.CrossBuilding

sbtPlugin := true

scalacOptions := Seq("-deprecation", "-encoding", "utf8")

///////////////
// ls-sbt
///////////////

seq(lsSettings :_*)

(LsKeys.tags in LsKeys.lsync) := Seq("sbt-plugin", "sbt", "plugin", "jrebel")

(LsKeys.docsUrl in LsKeys.lsync) <<= homepage

crossBuildingSettings

CrossBuilding.crossSbtVersions := Seq("0.13", "1.0.0-M4")

CrossBuilding.scriptedSettings
