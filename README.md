_sbt-revolver_ is a plugin for [SBT] enabling a super-fast development turnaround for your Scala applications.

It sports the following features:

* Starting and stopping your application in the background of your interactive SBT shell (in a forked JVM)
* Triggered restart: automatically restart your application as soon as some of its sources have been changed

Even though _sbt-revolver_ works great with [spray] on [spray-can] there is nothing _spray_-specific to it. It can
be used with any Scala application as long as there is some object with a `main` method.


## Installation

_sbt-revolver_ requires [SBT] 0.13.x or greater. Add the following dependency to your `project/plugins.sbt`:

```scala
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.0")
```

sbt-revolver is an auto plugin, so you don't need any additional configuration in your build.sbt nor in Build.scala
to make it work. In multi-module builds it will be enabled for each module. To disable sbt-revolver for some submodules use `Project(...).disablePlugins(RevolverPlugin)` in your build file.

For older versions of sbt see version [0.7.2](https://github.com/spray/sbt-revolver/tree/571ca53a5a2d93764774bb87cd96dad0ad0547b3).

## Usage

_sbt-revolver_ defines three new commands (SBT tasks) in its own `re` configuration:

* `reStart <args> --- <jvmArgs>` starts your application in a forked JVM.
  The optionally specified (JVM) arguments are appended to the ones configured via the `reStartArgs`/
  `reStart::javaOptions` setting (see the "Configuration" section below). If the application is already running it
  is first stopped before being restarted.

* `reStop` stops application.
  This is done by simply force-killing the forked JVM. Note, that this means that [shutdown hooks] are not run (see
  [#20](https://github.com/spray/sbt-revolver/issues/20)).

* `reStatus` shows an informational message about the current running state of the application.

#### Triggered Restart

You can use `~reStart` to go into "triggered restart" mode. Your application starts up and SBT watches for changes in
your source (or resource) files. If a change is detected SBT recompiles the required classes and _sbt-revolver_
automatically restarts your application.
When you press &lt;ENTER&gt; SBT leaves "triggered restart" and returns to the normal prompt keeping your application running.

To customize which files should be watched for triggered restart see the sbt documentation about [Triggered Execution](http://www.scala-sbt.org/0.13/docs/Triggered-Execution.html).

## Configuration

The following SBT settings defined by _sbt-revolver_ are of potential interest:

* `reStartArgs`, a `SettingKey[Seq[String]]`, which lets you define arguments that _sbt-revolver_ should pass to your
  application on every start. Any arguments given to the `reStart` task directly will be appended to this setting.
* `reStart::mainClass`, which lets you optionally define a main class to run in `reStart` independently of the
  one set for running the project normally. This value defaults to the value of `compile:run::mainClass`. If you
  don't specify a value here explicitly the same logic as for the normal run main class applies: If only one main class
  is found it one is chosen. Otherwise, the main-class chooser is shown to the user.
* `reStart::javaOptions`, a `SettingKey[Seq[String]]`, which lets you define the options to pass to the forked JVM
  when starting your application
* `reStart::baseDirectory`, a `SettingKey[File]`, which lets you customize the base directory independently from
  what `run` assumes.
* `reStart::fullClasspath`, which lets you customize the full classpath path for running with `reStart`.
* `reStart::envVars`, which lets you customize the environment variables for running the application.
* `reJrebelJar`, a `SettingKey[String]`, which lets you override the value of the `JREBEL_PATH` env variable.
* `reJrebelAgent`, a `SettingKey[String]`, which lets you override the value of the `JREBEL_AGENT_PATH` env variable.
* `reColors`, a `SettingKey[Seq[String]]`, which lets you change colors used to tag output from running processes.
  There are some pre-defined color schemes, see the example section below.
* `reLogTag`, a `SettingKey[String]`, which lets you change the log tag shown in front of log messages. Default is the
  project name.
* `debugSettings`, a `SettingKey[Option[DebugSettings]]` to specify remote debugger settings. There's a convenience
  helper `Revolver.enableDebugging` to simplify to enable debugging (see examples).

Examples:

To configure a 2 GB memory limit for your app when started with `reStart`:

    javaOptions in reStart += "-Xmx2g"

To set a special main class for your app when started with `reStart`:

    mainClass in reStart := Some("com.example.Main")

To set fixed start arguments (than you can still append to with the `reStart` task):

    reStartArgs := Seq("-x")

To enable debugging with the specified options:

    Revolver.enableDebugging(port = 5050, suspend = true)

To change set of colors used to tag output from multiple processes:

    reColors := Seq("blue", "green", "magenta")

There are predefined color schemes to use with `reColors`: `Revolver.noColors`, `Revolver.basicColors`,
`Revolver.basicColorsAndUnderlined`.

To add environment variables when running the application:

    envVars in reStart := Map("USER_TOKEN" -> "2359298356239")

#### Hot Reloading

*Note: JRebel support in sbt-revolver is not actively supported any more.*

If you have JRebel installed you can let _sbt-revolver_ know where to find the `jrebel.jar` or the agent libraries. You can do this
either via the `Revolver.jRebelJar`/`Revolver.jRebelAgent` setting directly in your SBT config or via a shell environment variable with the
name `JREBEL_PATH` or `JREBEL_AGENT_PATH` (which is the recommended way, since it doesn't pollute your SBT config with system-specific settings).
For example, on OSX you would add the following line to your shell startup script:

    # if you want to use the older jrebel. jar
    export JREBEL_PATH=/Applications/ZeroTurnaround/JRebel/jrebel.jar

    # or for newer JRebel versions
    export JREBEL_AGENT_PATH=/Applications/ZeroTurnaround/JRebel/lib/libjrebel64.dylib

With JRebel _sbt-revolver_ supports hot reloading:

* Start your application with `reStart`.
* Enter "triggered compilation" with `~products`. SBT watches for changes in your source (and resource) files.
  If a change is detected SBT recompiles the required classes and JRebel loads these classes right into your running
  application. Since your application is not restarted the time required to bring changes online is minimal (see
  the "Understanding JRebel" section below for more details). When you press &lt;ENTER&gt; SBT leaves triggered compilation
  and returns to the normal prompt keeping your application running.
* If you changed your application in a way that requires a full restart (see below) press &lt;ENTER&gt; to leave
  triggered compilation and `reStart`.
* Of course you always stop the application with `reStop`.

## License

_sbt-revolver_ is licensed under [APL 2.0].


## Patch Policy

Feedback and contributions to the project, no matter what kind, are always very welcome.
However, patches can only be accepted from their original author.
Along with any patches, please state that the patch is your original work and that you license the work to the
_sbt-revolver_ project under the project’s open source license.


  [SBT]: https://github.com/harrah/xsbt/wiki
  [JRebel]: http://zeroturnaround.com/software/jrebel/
  [xsbt-web-plugin]: https://github.com/aolshevskiy/xsbt-web-plugin
  [spray]: http://spray.io
  [spray-can]: https://github.com/spray/spray-can
  [shutdown hooks]: http://docs.oracle.com/javase/6/docs/api/java/lang/Runtime.html#addShutdownHook(java.lang.Thread)
  [JRebel FAQ]: http://zeroturnaround.com/software/jrebel/learn/faq/
  [APL 2.0]: http://www.apache.org/licenses/LICENSE-2.0
