ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.19"
ThisBuild / crossScalaVersions := Seq("2.12.19", "2.13.14", "3.4.2")

lazy val dependencies = Seq(
  "net.ruippeixotog" %% "scala-scraper" % "3.1.1",
  "org.jsoup" % "jsoup" % "1.17.2",
  "com.google.guava" % "guava" % "31.1-jre"
)
lazy val root = (project in file("."))
  .settings(
    name := "ScalaScraperJLSTest",
    libraryDependencies ++= dependencies
  )
