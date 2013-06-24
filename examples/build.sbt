name := "computation-graph-examples"

version := "0.1"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
	"org.scala-lang" % "scala-compiler" % "2.10.2",
	"org.scala-lang" % "scala-reflect" % "2.10.2",
	"org.scalaz" %% "scalaz-core" % "7.0.0",
	"org.clapper" %% "grizzled-slf4j" % "1.0.1",
	"ch.qos.logback" % "logback-classic" % "1.0.7",
	"org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
	"com.assembla.scala-incubator" % "graph-core_2.10" % "1.6.1"
)

//---
// JavaFX
//
// Note: We shouldn't even need to say this at all. Part of Java 7 RT (since 7u06) and should come from there (right)
//      The downside is that now this also gets into the 'one-jar' .jar package (where it would not need to be,
//      and takes 15MB of space - of the 17MB package!) AKa 1-Nov-2012
//
//unmanagedJars in Compile <+= javaHome map { jh /*: Option[File]*/ =>
//  val dir: File = jh.getOrElse(null)    // unSome
//  //
//  val jfxJar = new File(dir, "/jre/lib/jfxrt.jar")
//  if (!jfxJar.exists) {
//    throw new RuntimeException( "JavaFX not detected (needs Java runtime 7u06 or later): "+ jfxJar.getPath )  // '.getPath' = full filename
//  }
//  Attributed.blank(jfxJar)
//}
