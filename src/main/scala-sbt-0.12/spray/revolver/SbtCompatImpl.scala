package spray.revolver

import sbt._
import Keys._

object SbtCompatImpl extends SbtCompat with RevolverKeys {
  def changeJavaOptionsWithExtra[T](extra: SettingKey[T])(f: (Seq[String], String, T) => Seq[String]): Setting[_] =
    javaOptions in reStart <<= (javaOptions, reJRebelJar, extra) map f
}
