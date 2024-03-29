name := "scalethin"

version := "1.1.0"

scalaVersion := "2.11.12"

val akkaVersion = "2.5.12"
val akkaHttpVersion = "10.1.1"
val json4sVersion = "3.5.3"

libraryDependencies ++= Seq(
  "com.softwaremill.sttp" %% "core" % "1.3.0",
  "org.json4s" %% "json4s-jackson" % json4sVersion,
  "org.json4s" %% "json4s-core" % json4sVersion
)