name := "computation-graph"

version := "0.1"

scalaVersion := "2.10.1"

libraryDependencies ++= Seq(
	"org.scala-lang" % "scala-compiler" % "2.10.1",
	"org.scala-lang" % "scala-reflect" % "2.10.1",
	"org.scalaz" %% "scalaz-core" % "7.0.0",
	"org.clapper" %% "grizzled-slf4j" % "1.0.1",
	"ch.qos.logback" % "logback-classic" % "1.0.7",
	"org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
	"org.scalafx" % "scalafx_2.10" % "1.0.0-M4"
	"org.scala-lang" % "scala-swing" % "2.10.1"
)
