import play.sbt.PlayImport.PlayKeys.devSettings

name := """play-petclinic"""

version := "2.6.x"

def gatlingVersion(scalaBinVer: String): String = scalaBinVer match {
  case "2.11" => "2.2.5"
  case "2.12" => "2.3.0"
}

inThisBuild(
  List(
    scalaVersion := "2.12.4",
    crossScalaVersions := Seq("2.11.12", "2.12.4"),
    dependencyOverrides := Seq(
      "org.codehaus.plexus" % "plexus-utils" % "3.0.18",
      "com.google.code.findbugs" % "jsr305" % "3.0.1",
      "com.google.guava" % "guava" % "22.0"
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature"
      //other options
    )
  )
)


lazy val GatlingTest = config("gatling") extend Test

lazy val root = (project in file(".")).enablePlugins(PlayScala, GatlingPlugin).configs(GatlingTest)
  .settings(inConfig(GatlingTest)(Defaults.testSettings): _*)
  .settings(
    scalaSource in GatlingTest := baseDirectory.value / "/gatling/simulation"
  )
  .settings(devSettings ++= Seq("config.resource" -> "local.conf"))
  .settings(commonSettings: _*)
  .dependsOn(common, petclinic, auth, billing)

lazy val common = (project in file("modules/common"))
  .settings(name := "common")
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)

lazy val petclinic = (project in file("modules/petclinic"))
    .settings(name := "petclinic")
    .enablePlugins(PlayScala)
    .settings(commonSettings: _*)
    .dependsOn(common % "compile->compile;test->test")

lazy val auth = (project in file("modules/auth"))
      .settings(name := "auth")
      .enablePlugins(PlayScala)
      .settings(commonSettings: _*)
      .dependsOn(common % "compile->compile;test->test")

lazy val billing = (project in file("modules/billing"))
  .settings(name := "billing")
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .dependsOn(common % "compile->compile;test->test")

lazy val commonSettings = Seq(
  organization := "com.dominikdorn.play-petclinic",
  version := "1.0.0-SNAPSHOT",
  scalacOptions ++= Seq(
    "-target:jvm-1.8",
    "-Xmax-classfile-name", "120",
    "-unchecked",
    "-Xfatal-warnings",
    "-language:reflectiveCalls",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-feature",
    "-deprecation",
    "-Xlint",
    "-Xlint:-package-object-classes",
    "-Xlint:-missing-interpolator",
    "-Xlint:doc-detached",
    "-Xlint:-unused"
  ),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  scalaVersion := "2.12.4",
  logBuffered := true,
  sources in(Compile, doc) := Seq.empty,
  publishArtifact in(Compile, packageDoc) := false,
  publishArtifact in(Compile, packageSrc) := false,

  libraryDependencies += guice
  , libraryDependencies += javaJpa
  , libraryDependencies += "com.h2database" % "h2" % "1.4.196"

  , libraryDependencies += "org.hibernate" % "hibernate-core" % "5.2.9.Final"
  , libraryDependencies += "io.dropwizard.metrics" % "metrics-core" % "3.2.1"
  , libraryDependencies += "com.palominolabs.http" % "url-builder" % "1.1.0"
  , libraryDependencies += "net.jodah" % "failsafe" % "1.0.3"


  , libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion(scalaBinaryVersion.value) % Test
  , libraryDependencies += "io.gatling" % "gatling-test-framework" % gatlingVersion(scalaBinaryVersion.value) % Test

  //jackson scala module
  , libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.8"
  // akka persistence
  , libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % "2.5.10"
  , libraryDependencies += "com.typesafe.akka" %% "akka-typed" % "2.5.5"

  // query the journal
  , libraryDependencies += "com.typesafe.akka" %% "akka-persistence-query" % "2.5.10"
  , libraryDependencies += "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
  , PlayKeys.externalizeResources := false

  , testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))

)

