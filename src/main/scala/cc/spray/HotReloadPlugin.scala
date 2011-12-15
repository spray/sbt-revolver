package cc.spray

import sbt._
import sbt.Keys._

object HotReloadPlugin extends Plugin {

  object HotReload extends HotReloadKeys {
    import Actions._
    import Utilities._

    lazy val settings = Seq(

      // copied almost verbatim from the SBT sources.
      forkOptions <<= (taskTemporaryDirectory, scalaInstance, baseDirectory, startJavaOptions, outputStrategy, javaHome) map {
        (tmp, si, base, options, strategy, javaHomeDir) => ForkOptions(
          scalaJars = si.jars,
          javaHome = javaHomeDir,
          connectInput = false,
          outputStrategy = strategy,
          runJVMOptions = options,
          workingDirectory = Some(base)
        )
      },

      // default: no arguments to the app
      startArgs in Global := Seq.empty,

      start <<= inputTask { args =>
        (streams, state, forkOptions, mainClass in Compile, fullClasspath in Runtime, startArgs, args)
          .map(restartApp)
          .updateState(registerAppProcess)
          .dependsOn(products in Compile)
      },

      stop <<= (streams, state)
          .map(stopAppWithStreams)
          .updateState(unregisterAppProcess),

      // stop a possibly running application if the project is reloaded and the state is reset
      onUnload in Global ~= { onUnload => state =>
          if (state.has(appProcessKey)) stopApp(colorLogger(state), state)
          onUnload(state)
      },

      jRebelJar in Global := Option(System.getenv("JREBEL_PATH")).getOrElse(""),

      startJavaOptions <<= (javaOptions, streams, jRebelJar) map { (options, streams, jRebelJar) =>
        options ++ createJRebelAgentOption(streams.log, jRebelJar).toSeq
      }
    )
  }

}
