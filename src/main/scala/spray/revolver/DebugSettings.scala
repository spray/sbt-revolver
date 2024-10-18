package spray.revolver

case class DebugSettings(port: Int = 5005, suspend: Boolean = false, host: String = "localhost") {
  def toCmdLineArg: String =
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=%s,address=%s:%d".format(b2str(suspend), host, port)

  private def b2str(b: Boolean) = if (b) "y" else "n"
}
