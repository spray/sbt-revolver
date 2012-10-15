package spray.revolver

import sbt._

/**
 * Sbt compatibility code concrete instances are declared in sbt version specific folders
 */
trait SbtCompat {
  /**
   * Changes javaOptions by using transformator function
   * (javaOptions, jrebelJarPath) => newJavaOptions
   */
  def changeJavaOptions(f: (Seq[String], String) => Seq[String]): Setting[_]
}

object SbtCompat {
  def impl: SbtCompat = SbtCompatImpl
}