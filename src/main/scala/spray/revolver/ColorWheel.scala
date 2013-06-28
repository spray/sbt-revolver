package spray.revolver

case class ColorWheel(colors: IndexedSeq[String], nextIdx: Int = 0) {
  require(nextIdx >= 0 && nextIdx < colors.length)

  def grabColor: (String, ColorWheel) = {
    val color = colors(nextIdx)
    val idx = (nextIdx + 1) % colors.length
    (color, copy(nextIdx = idx))
  }
}
object ColorWheel {
  val simple = ColorWheel(Utilities.colors.toIndexedSeq)
}
