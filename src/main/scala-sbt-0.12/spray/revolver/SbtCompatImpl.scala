package spray.revolver

import sbt._
import Keys._

object SbtCompatImpl extends SbtCompat with RevolverKeys {
  def changeJavaOptions(f: (Seq[String], String) => Seq[String]) =
    javaOptions in reStart <<= (javaOptions, reJRebelJar) map f
}