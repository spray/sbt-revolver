/*
 * Copyright (C) 2009-2012 Johannes Rudolph and Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spray.revolver

import sbt._
import sbt.Keys._
import Actions._
import Utilities._
object RevolverPlugin extends AutoPlugin {

    object autoImport extends RevolverKeys {
      object Revolver {
        def settings = RevolverPlugin.settings

        def enableDebugging(port: Int = 5005, suspend: Boolean = false) =
          reStart / debugSettings := Some(DebugSettings(port, suspend))

        def noColors: Seq[String] = Nil
        def basicColors = Seq("BLUE", "MAGENTA", "CYAN", "YELLOW", "GREEN")
        def basicColorsAndUnderlined = basicColors ++ basicColors.map("_"+_)
      }

      val revolverSettings = RevolverPlugin.settings
    }
    import autoImport._

    lazy val settings = Seq(

      reStart / mainClass := (Compile / run / mainClass).value,

      reStart / fullClasspath := (Runtime / fullClasspath).value,

      Global / reStart / reColors := Revolver.basicColors,

      reStart := Def.inputTask{
        restartApp(
          streams.value,
          reLogTag.value,
          thisProjectRef.value,
          reForkOptions.value,
          (reStart / mainClass).value,
          (reStart / fullClasspath).value,
          reStartArgs.value,
          startArgsParser.parsed
        )
      }.dependsOn(Compile / products).evaluated,

      reStop := stopAppWithStreams(streams.value, thisProjectRef.value),

      reStatus := showStatus(streams.value, thisProjectRef.value),

      // default: no arguments to the app
      Global / reStartArgs := Seq.empty,

      // initialize with env variable
      Global / reJRebelJar := Option(System.getenv("JREBEL_PATH")).getOrElse(""),

      Global / debugSettings := None,

      reLogTagUnscoped := thisProjectRef.value.project,

      // bake JRebel activation into java options for the forked JVM
      changeJavaOptionsWithExtra(reStart / debugSettings) { (jvmOptions, jrJar, debug) =>
        jvmOptions ++ createJRebelAgentOption(SysoutLogger, jrJar).toSeq ++
          debug.map(_.toCmdLineArg).toSeq
      },

      // bundles the various parameters for forking
      reForkOptions := {
        taskTemporaryDirectory.value
        ForkOptions(
          javaHome = javaHome.value,
          outputStrategy = outputStrategy.value,
          bootJars = Vector.empty[File], // bootJars is empty by default because only jars on the user's classpath should be on the boot classpath
          workingDirectory = Option((reStart / baseDirectory).value),
          runJVMOptions = (reStart / javaOptions).value.toVector,
          connectInput = false,
          envVars = (reStart / envVars).value
        )
      },

      // stop a possibly running application if the project is reloaded and the state is reset
      Global / onUnload ~= { onUnload => state =>
        stopApps(colorLogger(state))
        onUnload(state)
      },

      Global / onLoad := { state =>
        val colorTags = (reStart / reColors).value.map(c => "[%s]".format(c.toUpperCase))
        GlobalState.update(_.copy(colorPool = collection.immutable.Queue(colorTags: _*)))
        (Global / onLoad).value.apply(state)
      }
    )

    override def requires = sbt.plugins.JvmPlugin
    override def trigger  = allRequirements
    override def projectSettings = settings

  /**
   * Changes javaOptions by using transformer function
   * (javaOptions, jrebelJarPath) => newJavaOptions
   */
  def changeJavaOptions(f: (Seq[String], String) => Seq[String]): Setting[_] =
    changeJavaOptionsWithExtra(sbt.Keys.baseDirectory /* just an ignored dummy */)((jvmArgs, path, _) => f(jvmArgs, path))

  def changeJavaOptionsWithExtra[T](extra: SettingKey[T])(f: (Seq[String], String, T) => Seq[String]): Setting[_] =
    reStart / javaOptions := f(javaOptions.value, reJRebelJar.value, extra.value)
}
