sbtPlugin := true

scalacOptions := Seq("-deprecation", "-encoding", "utf8")

crossSbtVersions := Vector("0.13.18", "1.2.8")

// Scripted test options.

scriptedSettings
scriptedLaunchOpts += s"-Dplugin.version=${version.value}"
scriptedBufferLog := false
test in Test := (test in Test).dependsOn(scripted.toTask("")).value
