name := """play-java-rest-api-example"""

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

lazy val root = (project in file(".")).enablePlugins(PlayJava, GatlingPlugin).configs(GatlingTest)
  .settings(inConfig(GatlingTest)(Defaults.testSettings): _*)
  .settings(
    scalaSource in GatlingTest := baseDirectory.value / "/gatling/simulation"
  )

libraryDependencies += guice
libraryDependencies += javaJpa
libraryDependencies += "com.h2database" % "h2" % "1.4.196"

libraryDependencies += "org.hibernate" % "hibernate-core" % "5.2.9.Final"
libraryDependencies += "io.dropwizard.metrics" % "metrics-core" % "3.2.1"
libraryDependencies += "com.palominolabs.http" % "url-builder" % "1.1.0"
libraryDependencies += "net.jodah" % "failsafe" % "1.0.3"


libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion(scalaBinaryVersion.value) % Test
libraryDependencies += "io.gatling" % "gatling-test-framework" % gatlingVersion(scalaBinaryVersion.value) % Test



//jackson scala module
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.8"
// akka persistence
libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % "2.5.10"

// query the journal
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-query" % "2.5.10"

libraryDependencies += "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"

PlayKeys.externalizeResources := false

testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))
