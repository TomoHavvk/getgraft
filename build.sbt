name := "snwatcher"

version := "0.1"

scalaVersion := "2.12.8"

val akkaHttpVersion = "10.1.3"
val akkaVersion = "2.5.13"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  "net.debasishg" %% "redisclient" % "3.9",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.13",
  "io.prometheus" % "simpleclient_common" % "0.5.0",
  "io.prometheus" % "simpleclient_hotspot" % "0.5.0",
  "io.prometheus" % "simpleclient_logback" % "0.5.0",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.9.8",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.8",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.8",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
)

mainClass in assembly := Some("com.tomohavvk.snwatcher.Boot")
