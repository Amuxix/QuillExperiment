name := "QuillExperiment"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "io.getquill" %% "quill-core" % "3.5.3",
  "io.getquill" %% "quill-async-postgres" % "3.5.3",
  "io.getquill" %% "quill-jdbc" % "3.5.3",
  "org.postgresql" % "postgresql" % "42.2.18.jre7",
  "org.typelevel" %% "cats-effect" % "2.2.0",
)