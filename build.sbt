organization := "eu.getintheloop"

name := "helix"

version := "0.1"

scalaVersion := "2.9.0-1"

scalacOptions += "-deprecation"

// "org.scalaquery" %% "scalaquery" % "0.9.5" % "compile",
// "mysql" % "mysql-connector-java" % "5.1.12" % "compile",
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile",
  "com.foursquare" %% "rogue" % "1.0.20" % "compile",
  "net.liftweb" %% "lift-webkit" % "2.4-M2" % "compile",
  "net.databinder" %% "dispatch-http" % "0.8.5",
  "net.databinder" %% "dispatch-json" % "0.8.5",
  "net.databinder" %% "dispatch-lift-json" % "0.8.5",
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "jetty"
)

resolvers += Resolver.file(".m2", file(Path.userHome+"/.m2/repository"))

seq(com.github.siasia.WebPlugin.webSettings :_*)

seq(bees.RunCloudPlugin.deploymentSettings :_*)
