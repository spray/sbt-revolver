package spray.revolver

import sbt._
import sbt.Keys._

trait RevolverPluginCompat {
  val reStartClasspathFilesTask = Def.task {
    val conv = fileConverter.value
    (Runtime / fullClasspath).value.map(f => conv.toPath(f.data).toFile)
  }
}
