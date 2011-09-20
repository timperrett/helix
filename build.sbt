organization := "eu.getintheloop"

name := "helix"

version := "0.1"

scalaVersion := "2.9.0-1"

scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  "se.scalablesolutions.akka" % "akka-actor" % "1.1.3" % "compile",
  "se.scalablesolutions.akka" % "akka-stm" % "1.1.3" % "compile",
  "org.multiverse" % "multiverse-alpha" % "0.6.2" % "compile",
  "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile",
  "org.mongodb" % "mongo-java-driver" % "2.5.3" % "compile",
  "com.mongodb.casbah" %% "casbah-core" % "2.1.5.0" % "compile",
  "com.novus" %% "salat-util" % "0.0.8-SNAPSHOT" % "compile",
  "com.novus" %% "salat-core" % "0.0.8-SNAPSHOT" % "compile",
  "net.liftweb" %% "lift-textile" % "2.4-M2" % "compile",
  "net.liftweb" %% "lift-webkit" % "2.4-M2" % "compile",
  "net.liftweb" %% "lift-wizard" % "2.4-M2" % "compile",
  "net.databinder" %% "dispatch-http" % "0.8.5" % "compile",
  "net.databinder" %% "dispatch-json" % "0.8.5" % "compile",
  "net.databinder" %% "dispatch-lift-json" % "0.8.5" % "compile",
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "jetty"
)

resolvers ++= Seq(
  ".m2" at "file://"+Path.userHome+"/.m2/repository",
  "salat" at "http://repo.novus.com/snapshots/",
  "typesafe" at "http://repo.typesafe.com/typesafe/releases/"
)

seq(deploymentSettings :_*)

seq(webSettings :_*)

// jettyClasspaths <<= (jettyClasspaths, sourceDirectory).map((j, src) => j.copy(classpath = j.classpath +++ src / "development" / "resources"))

jettyScanDirs := Nil

temporaryWarPath <<= (sourceDirectory in Compile)(_ / "webapp")

