
addSbtPlugin("com.github.siasia" % "xsbt-web-plugin" % "0.1.2")

// addSbtPlugin("eu.getintheloop" %% "sbt-cloudbees-plugin" % "0.3.1")

resolvers ++= Seq(
  "sonatype.repo" at "https://oss.sonatype.org/content/groups/public",
  "web-plugin.repo" at "http://siasia.github.com/maven2")
