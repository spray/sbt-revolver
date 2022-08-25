package spray.revolver

case class DebugSettings(port: Int = 5005, suspend: Boolean = false) {
  def toCmdLineArg: String =
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=%s,address=*:%d".format(b2str(suspend), port)

  private def b2str(b: Boolean) = if (b) "y" else "n"
}
