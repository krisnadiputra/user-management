val ScalatraVersion = "2.6.5"

organization := "com.paidy"

name := "User Management"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.10"

resolvers += Classpaths.typesafeReleases

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % ScalatraVersion,
  "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
  "org.scalatra" %% "scalatra-json" % "2.6.5",
  "org.json4s" %% "json4s-native" % "3.6.7",
  "org.json4s" %% "json4s-jackson" % "3.6.7",
  "com.typesafe.slick" %% "slick" % "3.2.3",
  "org.xerial" % "sqlite-jdbc" % "3.30.1",
  "org.typelevel" %% "cats-core" % "2.0.0",
  "org.typelevel" %% "cats-kernel" % "2.0.0",
  "com.softwaremill.quicklens" %% "quicklens" % "1.4.11",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
  "org.eclipse.jetty" % "jetty-webapp" % "9.4.19.v20190610" % "container",
  "com.mchange" % "c3p0" % "0.9.5.2",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
)

enablePlugins(SbtTwirl)
enablePlugins(ScalatraPlugin)
