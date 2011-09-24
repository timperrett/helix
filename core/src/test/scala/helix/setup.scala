package helix.test

import helix.domain._
import helix.util.Hex

object Sandbox {
  private val repositories = List(
    "jboner/akka", "robey/kestrel", "scalatra/scalatra", 
    "harrah/xsbt", "twitter/gizzard", "lift/lift", "scalaz/scalaz", 
    "nkallen/querulous", "n8han/Unfiltered", "twitter/ostrich", 
    "szeiger/scala-query", "scalate/scalate", "timperrett/lift-shiro", 
    "mongodb/casbah", "eed3si9n/scalaxb", "n8han/conscript", "novus/salat", 
    "codahale/dropwizard", "mtkopone/scct", "djspiewak/anti-xml", 
    "typesafehub/sbteclipse", "jsuereth/scala-arm", "etorreborre/specs2", 
    "jdegoes/blueeyes")
  
  private val projects = for{
    r <- repositories
    name = r.split('/').last
  } yield Project(
      name = name, 
      headline = Some("in fringilla quis, ornare eu odio. Vestibulum a nulla vel est"), 
      description = Some("""Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
        Quisque nibh nisi, pulvinar vitae aliquam in, volutpat in sem. Integer posuere, 
        nisl quis pharetra vulputate, lectus quam bibendum risus, at malesuada lorem 
        augue sit amet diam. Etiam ligula mauris, vestibulum dignissim ullamcorper
        non, pellentesque vel odio. Nunc eget sollicitudin magna. Nam nisl est, 
        imperdiet sit amet dapibus in, dapibus in urna. Pellen"""), 
      sourceURL = Some("https://github.com/%s".format(r)),
      groupId = Some("com.sandbox"), 
      artifactId = Some(name), 
      repositoryURL = Some("http://scala-tools.org/repo-releases/"),
      versions = Map(Hex.encode("0.1".getBytes) -> "2.9.1.Final"),
      tags = List("some", "web", "thing").map(Tag(_)),
      addedBy = Some("timperrett")
    )
  
  def setup() {
    helix.async.Manager.start()
    projects.foreach(Service.createProject(_))
  }
  
  def teardown() {
    helix.async.Manager.stop()
  }
}