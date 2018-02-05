sbtPlugin := true

scalacOptions := Seq("-deprecation", "-encoding", "utf8")

crossSbtVersions := Vector("0.13.17", "1.0.4")

// Scripted test options.

scriptedSettings
scriptedLaunchOpts += s"-Dplugin.version=${version.value}"
scriptedBufferLog := false
test in Test := (test in Test).dependsOn(scripted.toTask("")).value
