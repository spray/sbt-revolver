resolvers ++= Seq(
  "less is" at "http://repo.lessis.me",
  "coda" at "http://repo.codahale.com",
  "staging" at "https://oss.sonatype.org/content/repositories/netvirtual-void-1005"
)

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.2")

addSbtPlugin("net.virtual-void" % "sbt-cross-building" % "0.8.1")
