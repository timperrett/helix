package helix.http.ui

import scala.xml.{NodeSeq,Text}
import net.liftweb._, 
  common.{Box,Empty,Full},
  util.Helpers._,
  http.{SHtml,S},
  wizard.Wizard
import helix.domain.Service._
import helix.domain._

object ProjectWizard extends Wizard with CommonScreens {
  val general = new HelixScreen {
    override def screenName = "Project Information"
    
    val sourceURL = builder("Github Repo URL", "",
      valMaxLen(150, "URL too long"),
      valRegex("^https://github.com/([a-zA-Z0-9-]+)/([a-z0-9-]+)$".r.pattern, 
               "Not a valid github project URL")
    ).help("e.g. https://github.com/n8han/unfiltered").make
    
    val name = builder("Project Name", "",
      valMinLen(3, "Name too short"),
      valMaxLen(50, "Name too long")
    ).help("e.g. Unfiltered OAuth").make
    
    val tags = builder("Project Tags", ""
      ).help("e.g. sbt, web, lift").make
    
    val headline = builder("Project Headline", "",
      valMinLen(5, "Headline too short"),
      valMaxLen(80, "Headline too long")
    ).help("e.g. A toolkit for servicing HTTP requests in Scala").make
    
    val description = new Field { 
      type ValueType = String
      override def name = "Description" 
      override implicit def manifest = buildIt[String] 
      override def default = ""
      override def helpAsHtml = Full(Text("You can use Textile markup"))
      override def validations = List(valMinLen(10, "Description too short"))
      override def toForm: Box[NodeSeq] = 
        SHtml.textarea(is, set _) % ("class" -> "xxlarge") % ("rows" -> "10")
    }
    
    // val publishesPOM_? = field("Published Binary?", true)
    
    override def nextScreen = publishing
  }
  
  val publishing = new HelixScreen {
    override def screenName = "Project Publishing"
    
    val groupId = builder("Group ID", "",
      valMinLen(5, "Group ID too short")
    ).help("e.g. net.databinder").make
    
    val artifactId = builder("Artifact ID", "",
      valMinLen(3, "ArtifactId too short")
    ).help("e.g. unfiltered-oauth").make
    
    private val regex = """^(http|https)\://[a-zA-Z0-9\-\.]+\.[a-zA-Z]{2,3}(:[a-zA-Z0-9]*)?/?([a-zA-Z0-9\-\._\?\,\'/\\\+&amp;%\$#\=~])*$""".r
    val repositoryURL = field("Repository URL", "http://scala-tools.org/repo-releases/",
      valRegex(regex.pattern, "Not a valid URL format: must be http/https"))
    
    override def nextScreen = versioning
  }
  
  val versioning = new AddProjectVersionScreen { }
  
  import akka.actor.Actor.registry.actorFor
  import helix.util.Hex
  import helix.http.Vars.CurrentContributor
  
  def finish(){
    // this assignment is a fucking mess. Sort it out boy'o!
    val vs: Map[String, String] = Map(
      Hex.encode(versioning.currentVersion.is.getBytes) -> 
      versioning.versions.get.filter(_._2 == true).map(_._1.asVersion).toList.reverse.mkString(", "))
    
    // assemble the users input into a project. mess-tastic.
    val proj = Project(
      name = general.name.is, 
      headline = Some(general.headline.is), 
      description = Some(general.description.is), 
      sourceURL = Some(general.sourceURL.is),
      groupId = Some(publishing.groupId.is), 
      artifactId = Some(publishing.artifactId.is), 
      repositoryURL = Some(publishing.repositoryURL.is),
      // versions = N,
      tags = general.tags.is.split(',').map(t => Tag(t.trim)).toList,
      addedBy = CurrentContributor.is.map(_.login))
    
    // add to the db
    if(createProject(proj)) S.redirectTo("/projects/%s/%s".format(publishing.groupId.is,publishing.artifactId.is))
    else S.error("Unable to add project. Please try again.")
  }
}
