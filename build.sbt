name := "aut-sys-project"

version := "0.1"

scalaVersion := "2.12.4"
compileOrder := CompileOrder.JavaThenScala

val scalaSuffix = "2.12"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % s"akka-actor_$scalaSuffix" % "2.5.9",
  "com.typesafe.akka" % s"akka-agent_$scalaSuffix" % "2.5.9",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test" 
)


