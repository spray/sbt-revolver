# sbt-hot-reload

A plugin that allows to start, stop, or restart the current project in the background
of your sbt shell. This enables a very fast turnaround especially for server projects.

Optionally, if you configure a JRebel installation, you can avoid the restart for most cases and
can just use `~ compile` to update your class files on-the-fly while your server is running.

This project was developed primarily for use with spray in mind but this plugin right now contains
no special functionality for spray and may used be more generally for all kinds of server projects.

## Requirements

* Simple Build Tool >= 0.11.1

## How To enable the plugin in your project

For sbt 0.11, add sbt-hot-reload as a dependency in `project/plugins.sbt`:

```scala
addSbtPlugin("cc.spray" % "sbt-hot-reload" % "0.5.0")
```

or, alternatively, in `project/project/build.scala`:

```scala
import sbt._

object Plugins extends Build {
  lazy val root = Project("root", file(".")) dependsOn(
    uri("git://github.com/spray/sbt-hot-reload#v0.5.0") // or another tag/branch/revision
  )
}
```

Then, add the following in your `build.sbt`:

```scala
seq(HotReload.hotReloadSettings: _*)
```

## Common usage

Use `~ hr:start` on the sbt shell to continuously recompile and restart your server process.

With JRebel enabled use `hr:start` to start the server and then `~ compile` afterwards. Changes to the
class files will then be automatically picked up by JRebel.

## Tasks

### start
Compiles the project and runs it in the background by forking a new JVM. If the project was started
before, it is automatically stopped before.

### stop
Stops the project. The default behaviour is to forcibly kill the background process.

## Configuration

### startArgs

Use `HotReload.startArgs += "-x"` to add arguments which should be passed to the server
process when started.

### jRebelJar

Use `HotReload.jRebelJar := "/home/user/opt/JRebel/jrebel.jar"` to provide
the location of your JRebel installation. When starting the server the JRebel java agent will
then be included.

## License

Published under the [Apache License 2.0](http://en.wikipedia.org/wiki/Apache_license).