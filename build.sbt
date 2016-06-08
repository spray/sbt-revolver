sbtPlugin := true

scalacOptions := Seq("-deprecation", "-encoding", "utf8")

// Scripted test options.

scriptedSettings
scriptedLaunchOpts += s"-Dplugin.version=${version.value}"
scriptedBufferLog := false
test in Test <<= (test in Test).dependsOn(scripted.toTask(""))
