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

object RevolverPlugin extends Plugin {

  object Revolver extends RevolverKeys {
    import Actions._
    import Utilities._

    lazy val settings = seq(

      mainClass in reStart <<= mainClass in run in Compile,

      reColors in Global in reStart := basicColors,

      reStart <<= InputTask(startArgsParser) { args =>
        (streams, reLogTag, thisProjectRef, reForkOptions, mainClass in reStart, fullClasspath in Runtime, reStartArgs, args)
          .map(restartApp)
          .dependsOn(products in Compile)
      },

      reStop <<= (streams, thisProjectRef).map(stopAppWithStreams),

      reStatus <<= (streams, thisProjectRef) map showStatus,

      // default: no arguments to the app
      reStartArgs := Seq.empty,

      // initialize with env variable
      reJRebelJar := Option(System.getenv("JREBEL_PATH")).getOrElse(""),

      debugSettings := None,

      reLogTag <<= thisProjectRef(_.project),

      // bake JRebel activation into java options for the forked JVM
      SbtCompat.impl.changeJavaOptionsWithExtra(debugSettings in reStart) { (jvmOptions, jrJar, debug) =>
        jvmOptions ++ createJRebelAgentOption(SysoutLogger, jrJar).toSeq ++
          debug.map(_.toCmdLineArg).toSeq
      },

      // bundles the various parameters for forking
      reForkOptions <<= (taskTemporaryDirectory, scalaInstance, baseDirectory, javaOptions in reStart, outputStrategy,
        javaHome) map ( (tmp, si, base, jvmOptions, strategy, javaHomeDir) =>
        ForkOptions(
          javaHomeDir,
          strategy,
          si.jars,
          workingDirectory = Some(base),
          runJVMOptions = jvmOptions,
          connectInput = false
        )
      ),

      // stop a possibly running application if the project is reloaded and the state is reset
      onUnload in Global ~= { onUnload => state =>
        stopApps(colorLogger(state))
        onUnload(state)
      },

      onLoad in Global <<= (onLoad in Global, reColors in reStart) { (onLoad, colors) => state =>
        val colorTags = colors.map(_.toUpperCase formatted "[%s]")
        GlobalState.update(_.copy(colorPool = collection.immutable.Queue(colorTags: _*)))
        onLoad(state)
      }
    )

    def enableDebugging(port: Int = 5005, suspend: Boolean = false) =
      debugSettings in reStart := Some(DebugSettings(port, suspend))

    def noColors: Seq[String] = Nil
    def basicColors = Seq("BLUE", "MAGENTA", "CYAN", "YELLOW", "RED", "GREEN")
    def basicColorsAndUnderlined = basicColors ++ basicColors.map("_"+_)
  }

}
