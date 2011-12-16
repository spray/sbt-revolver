/*
 * Copyright (C) 2009-2011 Johannes Rudolph and Mathias Doenitz
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

package cc.spray.revolver

import sbt._
import sbt.Keys._

object RevolverPlugin extends Plugin {

  object Revolver extends RevolverKeys {
    import Actions._
    import Utilities._

    lazy val settings = Seq(

      start <<= inputTask { args =>
        (streams, state, forkOptions, mainClass in Compile, fullClasspath in Runtime, startArgs, args)
          .map(restartApp)
          .updateState(registerAppProcess)
          .dependsOn(products in Compile)
      },

      stop <<= (streams, state)
          .map(stopAppWithStreams)
          .updateState(unregisterAppProcess),

      status <<= (streams, state) map showStatus,

      // default: no arguments to the app
      startArgs in Global := Seq.empty,

      // initialize with env variable
      jRebelJar in Global := Option(System.getenv("JREBEL_PATH")).getOrElse(""),

      // bake JRebel activation into java options for the forked JVM
      javaOptions in RE <<= (javaOptions, jRebelJar) { (jvmOptions, jrJar) =>
        jvmOptions ++ createJRebelAgentOption(SysoutLogger, jrJar).toSeq
      },

      // bundles the various parameters for forking
      forkOptions <<= (taskTemporaryDirectory, scalaInstance, baseDirectory, javaOptions in RE, outputStrategy, javaHome) map {
        (tmp, si, base, jvmOptions, strategy, javaHomeDir) => ForkOptions(
          scalaJars = si.jars,
          javaHome = javaHomeDir,
          connectInput = false,
          outputStrategy = strategy,
          runJVMOptions = jvmOptions,
          workingDirectory = Some(base)
        )
      },

      // stop a possibly running application if the project is reloaded and the state is reset
      onUnload in Global ~= { onUnload => state =>
        if (state.has(appProcessKey)) stopApp(colorLogger(state), state)
        onUnload(state)
      }
    )
  }

}
