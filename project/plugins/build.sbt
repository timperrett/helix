resolvers += "Web plugin repo" at "http://siasia.github.com/maven2"    

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % ("0.1.1-"+v))

resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

libraryDependencies += "com.github.mpeltonen" %% "sbt-idea" % "0.10.0"
