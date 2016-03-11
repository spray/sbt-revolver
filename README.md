_sbt-revolver_ is a plugin for [SBT] enabling a super-fast development turnaround for your Scala applications.

It supports the following features:

* Starting and stopping your application in the background of your interactive SBT shell (in a forked JVM)
* Triggered restart: automatically restart your application as soon as some of its sources have been changed

Even though _sbt-revolver_ works great with [spray] on [spray-can] there is nothing _spray_-specific to it. It can
be used with any Scala application as long as there is some object with a `main` method.


## Installation

_sbt-revolver_ requires [SBT] 0.13.x or greater. Add the following dependency to your `project/plugins.sbt`:

```scala
addSbtPlugin("io.spray" % "sbt-revolver" % "0.8.0")
```

sbt-revolver is an auto plugin, so you don't need any additional configuration in your build.sbt nor in Build.scala
to make it work. In multi-module builds it will be enabled for each module.

For older versions of sbt see version [0.7.2](https://github.com/spray/sbt-revolver/tree/571ca53a5a2d93764774bb87cd96dad0ad0547b3).

## Usage

_sbt-revolver_ defines three new commands (SBT tasks) in its own `re` configuration:

* `re-start <args> --- <jvmArgs>` starts your application in a forked JVM.
  The optionally specified (JVM) arguments are appended to the ones configured via the `re-start-args`/
  `java-options(for re-start)` setting (see the "Configuration" section below). If the application is already running it
  is first stopped before being restarted.

* `re-stop` stops application.
  This is done by simply force-killing the forked JVM. Note, that this means that [shutdown hooks] are not run (see
  [#20](http://github.com/spray/sbt-revolver/issues/20)).

* `re-status` shows an informational message about the current running state of the application.

#### Triggered Restart

You can use `~re-start` to go into "triggered restart" mode. Your application starts up and SBT watches for changes in
your source (or resource) files. If a change is detected SBT recompiles the required classes and _sbt-revolver_
automatically restarts your application.
When you press &lt;ENTER&gt; SBT leaves "triggered restart" and returns to the normal prompt keeping your application running.

## Configuration

The following SBT settings defined by _sbt-revolver_ are of potential interest:

* `re-start-args`, a `SettingKey[Seq[String]]`, which lets you define arguments that _sbt-revolver_ should pass to your
  application on every start. Any arguments given to the `re-start` task directly will be appended to this setting.
* `re-start::re-main-class`, which lets you optionally define a main class to run in `re-start` independently of the
  one set for running the project normally. This value defaults to the value of `compile:main-class(for run)`. If you
  don't specify a value here explicitly the same logic as for the normal run main class applies: If only one main class
  is found it one is chosen. Otherwise, the main-class chooser is shown to the user.
* `re-start::java-options`, a `SettingKey[Seq[String]]`, which lets you define the options to pass to the forked JVM
  when starting your application
* `re-start::base-directory`, a `SettingKey[File]`, which lets you customize the base directory independently from
  what `run` assumes.
* `re-start::full-classpath`, which lets you customize the full classpath path for running with `re-start`.
* `re-jrebel-jar`, a `SettingKey[String]`, which lets you override the value of the `JREBEL_PATH` env variable.
* `re-colors`, a `SettingKey[Seq[String]]`, which lets you change colors used to tag output from running processes.
  There are some pre-defined color schemes, see the example section below.
* `re-log-tag`, a `SettingKey[String]`, which lets you change the log tag shown in front of log messages. Default is the
  project name.
* `debug-settings`, a `SettingKey[Option[DebugSettings]]` to specify remote debugger settings. There's a convenience
  helper `Revolver.enableDebugging` to simplify to enable debugging (see examples).

Examples:

To configure a 2 GB memory limit for your app when started with `re-start`:

    javaOptions in reStart += "-Xmx2g"

To set a special main class for your app when started with `re-start`:

    mainClass in reStart := Some("com.example.Main")

To set fixed start arguments (than you can still append to with the `re-start` task):

    reStartArgs := Seq("-x")

To enable debugging with the specified options:

    Revolver.enableDebugging(port = 5050, suspend = true)

To change set of colors used to tag output from multiple processes:

    reColors := Seq("blue", "green", "magenta")

There are predefined color schemes to use with `reColors`: `Revolver.noColors`, `Revolver.basicColors`,
`Revolver.basicColorsAndUnderlined`.

#### Hot Reloading

*Note: JRebel support in sbt-revolver is not actively supported any more.*

If you have JRebel installed you can let _sbt-revolver_ know where to find the `jrebel.jar`. You can do this
either via the `Revolver.jRebelJar` setting directly in your SBT config or via a shell environment variable with the
name `JREBEL_PATH` (which is the recommended way, since it doesn't pollute your SBT config with system-specific settings).
For example, on OSX you would add the following line to your shell startup script:

    export JREBEL_PATH=/Applications/ZeroTurnaround/JRebel/jrebel.jar

With JRebel _sbt-revolver_ supports hot reloading:

* Start your application with `re-start`.
* Enter "triggered compilation" with `~products`. SBT watches for changes in your source (and resource) files.
  If a change is detected SBT recompiles the required classes and JRebel loads these classes right into your running
  application. Since your application is not restarted the time required to bring changes online is minimal (see
  the "Understanding JRebel" section below for more details). When you press &lt;ENTER&gt; SBT leaves triggered compilation
  and returns to the normal prompt keeping your application running.
* If you changed your application in a way that requires a full restart (see below) press &lt;ENTER&gt; to leave
  triggered compilation and `re-start`.
* Of course you always stop the application with `re-stop`.

## License

_sbt-revolver_ is licensed under [APL 2.0].


## Patch Policy

Feedback and contributions to the project, no matter what kind, are always very welcome.
However, patches can only be accepted from their original author.
Along with any patches, please state that the patch is your original work and that you license the work to the
_sbt-revolver_ project under the project’s open source license.


  [SBT]: https://github.com/harrah/xsbt/wiki
  [JRebel]: http://zeroturnaround.com/jrebel/
  [xsbt-web-plugin]: https://github.com/siasia/xsbt-web-plugin/
  [spray]: http://spray.io
  [spray-can]: https://github.com/spray/spray-can
  [shutdown hooks]: http://docs.oracle.com/javase/6/docs/api/java/lang/Runtime.html#addShutdownHook(java.lang.Thread)
  [JRebel FAQ]: http://zeroturnaround.com/jrebel/faq/
  [APL 2.0]: http://www.apache.org/licenses/LICENSE-2.0
