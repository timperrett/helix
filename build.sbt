organization := "eu.getintheloop"

name := "helix"

version := "0.1"

scalaVersion := "2.9.0-1"

scalacOptions += "-deprecation"

// "org.scalaquery" %% "scalaquery" % "0.9.5" % "compile",
// "mysql" % "mysql-connector-java" % "5.1.12" % "compile",
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile",
  "com.novus" %% "salat-util" % "0.0.8-SNAPSHOT",
  "com.novus" %% "salat-core" % "0.0.8-SNAPSHOT",
  "net.liftweb" %% "lift-webkit" % "2.4-M2" % "compile",
  "net.liftweb" %% "lift-wizard" % "2.4-M2" % "compile",
  "net.databinder" %% "dispatch-http" % "0.8.5",
  "net.databinder" %% "dispatch-json" % "0.8.5",
  "net.databinder" %% "dispatch-lift-json" % "0.8.5",
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "jetty"
)

resolvers ++= Seq(
  Resolver.file(".m2", file(Path.userHome+"/.m2/repository")),
  Resolver.url("salat", url("http://repo.novus.com/snapshots/"))
)

seq(deploymentSettings :_*)

seq(webSettings :_*)

jettyClasspaths <<= (jettyClasspaths, sourceDirectory).map((j, src) => j.copy(classpath = j.classpath +++ src / "development" / "resources"))

jettyScanDirs := Nil

temporaryWarPath <<= (sourceDirectory in Compile)(_ / "webapp")

