package spray.revolver

import sbt._
import sbt.Keys._

trait RevolverPluginCompat {
  val reStartClasspathFilesTask = Def.task {
    (Runtime / fullClasspath).value.map(_.data)
  }
}
