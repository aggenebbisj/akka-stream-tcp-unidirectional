name := "streams"

version := "1.0"

scalaVersion := "2.11.5"

val akkaVersion = "2.4.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka"         %% "akka-actor"                     % akkaVersion,
  "com.typesafe.akka"		      %% "akka-stream" 		                % akkaVersion
)
