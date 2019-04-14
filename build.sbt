name := "aut-sys-project"

version := "0.1"

scalaVersion := "2.12.4"
compileOrder := CompileOrder.JavaThenScala

val scalaSuffix = "2.12"

libraryDependencies ++= Seq(
  "javax.ws.rs" % "javax.ws.rs-api" % "2.0.1",
  "com.typesafe.akka" %% "akka-http" % "10.1.8",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8",
  "com.typesafe.akka" %% "akka-agent" % "2.5.9",
  "com.typesafe.akka" %% "akka-actor" % "2.5.9",
  "com.typesafe.akka" %% "akka-stream" % "2.5.9",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.9" % Test,
  // Swagger
  "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.0.2",
  "com.github.swagger-akka-http" %% "swagger-scala-module" % "2.0.3",
  // CORS
  "ch.megard" %% "akka-http-cors" % "0.4.0",
  "org.scalatest" %% "scalatest" % "3.0.4" % Test
)


