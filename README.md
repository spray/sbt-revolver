_sbt-revolver_ is a plugin for [SBT] enabling a super-fast development turnaround for your Scala applications.

It sports the following features:

* Starting and stopping your application in the background of your interactive SBT shell (in a forked JVM)
* Triggered restart: automatically restart your application as soon as some of its sources have been changed
* Hot reloading: automatically reload the respective classes into your running application as soon as some
  of its sources have been changed, no restart necessary (requires [JRebel], which is free for Scala development)

Even though _sbt-revolver_ works great with [spray] on [spray-can] there is nothing _spray_-specific to it. It can
be used with any Scala application as long as there is some object with a `main` method.


## Installation

_sbt-revolver_ requires [SBT] 0.11.1 or greater.
Add the following dependency to your `project/*.sbt` file (e.g. `project/plugins.sbt`):

```scala
resolvers += "spray repo" at "http://repo.spray.io" // not needed for sbt >= 0.12

addSbtPlugin("io.spray" % "sbt-revolver" % "0.6.2")
```

and this to your `build.sbt`:

```scala
seq(Revolver.settings: _*)
```

If you use SBTs full-configuration you need to

```scala
import spray.revolver.RevolverPlugin._
```

and then add the `Revolver.settings` to the (sub-)project containing the `main` object.

#### JRebel

In order to enable hot reloading you need get a licensed JRebel JAR onto your system.
You can do this with these simple steps:

1. Go to [this page](http://sales.zeroturnaround.com/) and apply for a free "JRebel for Scala" license
   (in the "Get JRebel for free" box on the right).
2. Wait for the email from ZeroTurnaround containing your personal `jrebel.lic` file.
3. Create a `~/.jrebel` directory and copy the `jrebel.lic` file from the email into this directory.
4. Download the JRebel _Generic JAR Installer_ from [this page](http://zeroturnaround.com/jrebel/current/) and run it.
   When asked for the license let the installer check your `~/.jrebel` directory. Remember which path the JRebel JAR
   was installed to.

Once you have JRebel installed you need to let _sbt-revolver_ know where to find the `jrebel.jar`. You can do this
either via the `Revolver.jRebelJar` setting directly in your SBT config or via a shell environment variable with the
name `JREBEL_PATH` (which is the recommended way, since it doesn't pollute your SBT config with system-specific settings).
For example, on OSX you would add the following line to your shell startup script:

    export JREBEL_PATH=/Applications/ZeroTurnaround/JRebel/jrebel.jar


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

#### Hot Reloading

When you have [JRebel] installed and configured as described in the "Installation" section above _sbt-revolver_ supports
hot reloading:

* Start your application with `re-start`.
* Enter "triggered compilation" with `~products`. SBT watches for changes in your source (and resource) files.
  If a change is detected SBT recompiles the required classes and JRebel loads these classes right into your running
  application. Since your application is not restarted the time required to bring changes online is minimal (see
  the "Understanding JRebel" section below for more details). When you press &lt;ENTER&gt; SBT leaves triggered compilation
  and returns to the normal prompt keeping your application running.
* If you changed your application in a way that requires a full restart (see below) press &lt;ENTER&gt; to leave
  triggered compilation and `re-start`.
* Of course you always stop the application with `re-stop`.


## Understanding JRebel

JRebel is a JVM `-javaagent` plugin that extends the root classloaders with the ability to manage reloaded classes.
When a change to a class file is detected on disk JRebel loads the changed class into your running application.
This is great for quickly bringing code changes live but also comes with some restriction that you should understand in
order to be able to use JRebel and _sbt-revolver_ effectively.

When JRebel reloads a class `Foo` all the code of `Foo` is updated to the new version. However, all instances of `Foo`
that already exist at the time of class reloading will remain alive in the JVMs heap. All fields of these objects
will also remain unchanged. After reloading all methods called on `Foo` objects will be changed to their new
implementations. If the new `Foo` class contains instance fields that were not present in the old `Foo` these instance
fields will **not be initialized** for all old `Foo` instances! Also, if you change the way that `Foo` instances are
initialized then this change will only apply to `Foo` instances created _after_ class reloading. All old instances that
have been initialized by the old `Foo` will remain unchanged.

The thing to remember when working with JRebel is that JRebel changes _code_, not data. All code running after
class reloading will be the new code. Any code having run before class reloading will not be run again.

More information about JRebel can be found in the [JRebel FAQ].


## Configuration

The following SBT settings defined by _sbt-revolver_ are of potential interest:

* `re-start-args`, a `SettingKey[Seq[String]]`, which lets you define arguments that _sbt-revolver_ should pass to your
  application on every start. Any arguments given to the `re-start` task directly will be appended to this setting.
* `re-main-class(for re-start)`, which lets you optionally define a main class to run in `re-start` independently of the
  one set for running the project normally. This value defaults to the value of `compile:main-class(for run)`. If you
  don't specify a value here explicitly the same logic as for the normal run main class applies: If only one main class
  is found it one is chosen. Otherwise, the main-class chooser is shown to the user.
* `java-options(for re-start)`, a `SettingKey[Seq[String]]`, which lets you define the options to pass to the forked JVM
  when starting your application
* `re-jrebel-jar`, a `SettingKey[String]`, which lets you override the value of the `JREBEL_PATH` env variable.


Examples:

To configure a 2 GB memory limit for your app when started with `re-start`:

    javaOptions in Revolver.reStart += "-Xmx2g"

To set a special main class for your app when started with `re-start`:

    mainClass in Revolver.reStart := Some("com.example.Main")

To set fixed start arguments (than you can still append to with the `re-start` task):

    Revolver.reStartArgs := Seq("-x")


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
