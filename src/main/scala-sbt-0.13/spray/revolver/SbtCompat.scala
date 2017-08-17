package spray.revolver

import sbt.ForkOptions

object SbtCompat {
  type Process = sbt.Process

  implicit class ForkOptionsCompatOps(val underlying: ForkOptions) extends AnyVal {
    def withRunJVMOptions(runJVMOptions: Seq[String]): ForkOptions =
      underlying.copy(runJVMOptions = runJVMOptions)

    def withOutputStrategy(outputStrategy: sbt.OutputStrategy): ForkOptions =
      underlying.copy(outputStrategy = Option(outputStrategy))
  }
}
