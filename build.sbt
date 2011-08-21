organization := "org.scalatools"

name := "alghanim"

version := "0.1"

scalaVersion := "2.9.0-1"

scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  "net.liftweb" %% "lift-webkit" % "2.4-M2" % "compile",
  "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile",
  "org.scalaquery" %% "scalaquery" % "0.9.5" % "compile",
  "mysql" % "mysql-connector-java" % "5.1.12" % "compile",
  "org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "jetty",
  "net.databinder" %% "dispatch-http" % "0.8.5",
  "net.databinder" %% "dispatch-json" % "0.8.5",
  "net.databinder" %% "dispatch-lift-json" % "0.8.5"
  // "net.databinder" %% "dispatch-lift-json" % "0.8.5",
)

resolvers += Resolver.file(".m2", file(Path.userHome+"/.m2/repository"))

seq(com.github.siasia.WebPlugin.webSettings :_*)
