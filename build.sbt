name := "ToDo"
version := "0.1"
scalaVersion := "2.12.4"
organization := "com.hwaipy"
scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8")
libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.102-R11"
// Fork a new JVM for 'run' and 'test:run', to avoid JavaFX double initialization problems
fork := true
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
scalacOptions in ThisBuild ++= Seq("-feature", "-deprecation")
// [Required] Enable plugin and automatically find def main(args:Array[String]) methods from the classpath
enablePlugins(PackPlugin)
