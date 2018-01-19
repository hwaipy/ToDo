name := "ToDo"
version := "0.1"
scalaVersion := "2.12.4"
organization := "com.hwaipy"
scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8")
libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.102-R11"
// Fork a new JVM for 'run' and 'test:run', to avoid JavaFX double initialization problems
fork := true