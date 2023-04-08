enablePlugins(SbtPlugin)

scalacOptions := Seq("-deprecation", "-encoding", "utf8")

scriptedLaunchOpts += s"-Dplugin.version=${version.value}"
scriptedBufferLog := false
Test / test := (Test / test).dependsOn(scripted.toTask("")).value
