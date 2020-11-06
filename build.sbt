name := "QuillExperiment"

version := "0.1"

scalaVersion := "2.13.3"

enablePlugins(JooqCodegenPlugin)

jooqCodegenConfig := file("src/main/resources/db/jooq-codegen.xml")
jooqVersion := "3.14.1"

val versionPostgresDriver = "42.2.18.jre7"

libraryDependencies ++= Seq(
  "io.getquill" %% "quill-core" % "3.5.3",
  "io.getquill" %% "quill-async-postgres" % "3.5.3",
  "io.getquill" %% "quill-jdbc" % "3.5.3",
  "org.jooq" %% "jooq-scala" % jooqVersion.value,
  "org.postgresql" % "postgresql" % versionPostgresDriver,
  "org.postgresql" % "postgresql" % versionPostgresDriver % JooqCodegen,
  "org.typelevel" %% "cats-effect" % "2.2.0",
)

lazy val db = project.in(file("db")).settings(
  libraryDependencies += "org.postgresql" % "postgresql" % versionPostgresDriver,
  flywayLocations := Seq("migrations"),
  flywayUrl := "jdbc:postgresql:test",
  flywayUser := "test",
  flywayPassword := "test",
).enablePlugins(FlywayPlugin)
