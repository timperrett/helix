package helix.snippet

import scala.xml.{NodeSeq,Text}
import net.liftweb._, 
  common.{Box,Empty,Full},
  util.Helpers._,
  http.{SHtml,S},
  wizard.Wizard

object ProjectWizard extends Wizard {
  trait HelixScreen extends Screen {
    override def screenTop = Full(<h2>{screenNameAsHtml}</h2>)
    implicit def strToNodeSeq(s: String): NodeSeq = Text(s)
  }
  
  val general = new HelixScreen {
    override def screenName = "Project Information"
    
    val sourceURL = builder("Github Repo URL", "",
      valMaxLen(150, "URL too long"),
      valRegex("^https://github.com/([a-z0-9-]+)/([a-z0-9-]+)$".r.pattern, 
               "Not a valid github project URL")
    ).help("e.g. https://github.com/n8han/unfiltered").make
    
    val name = builder("Project Name", "",
      valMinLen(3, "Name too short"),
      valMaxLen(50, "Name too long")
    ).help("e.g. Unfiltered OAuth").make
    
    val groupId = builder("Group ID", "",
      valMinLen(5, "Group ID too short")
    ).help("e.g. net.databinder").make
    
    val artifactId = builder("Artifact ID", "",
      valMinLen(3, "ArtifactId too short")
    ).help("e.g. unfiltered-oauth").make
    
    val description = new Field { 
      type ValueType = String
      override def name = "Description" 
      override implicit def manifest = buildIt[String] 
      override def default = ""
      override def validations = List(valMinLen(10, "Description too short"))
      override def toForm: Box[NodeSeq] = 
        SHtml.textarea(is, set _) % ("class" -> "xlarge")
    }
    
    val publishesPOM_? = field("Published POM?", true)
    
    override def nextScreen = 
      if(publishesPOM_?) dependencies
      else versioning
      
    // val githubURL = new Field { 
    //   type ValueType = String
    //   override def name = "Github URL"
    //   override def validations = List(valMinLen(2, "Name Too Short"))
    //   override implicit def manifest = buildIt[String] 
    //   override def default = ""
    //   override def helpAsHtml = Full(Text("e.g. n8han/unfiltered"))
    //   override def toForm: Box[NodeSeq] = Full({
    //     <div class="input-prepend">
    //       <span class="add-on">github.com/</span>
    //       {SHtml.text(is, set _) % ("class" -> "large")}
    //       <span class="help-inline" id="loading">
    //         <img src="/images/loading.gif" width="18" height="18" />
    //       </span>
    //     </div>
    //   })
    // }
  }
  
  val dependencies = new HelixScreen {
    override def screenName = "Project Dependencies"
      
    private val regex = """^(http|https)\://[a-zA-Z0-9\-\.]+\.[a-zA-Z]{2,3}(:[a-zA-Z0-9]*)?/?([a-zA-Z0-9\-\._\?\,\'/\\\+&amp;%\$#\=~])*$""".r
    val repositoryURL = field("Repository URL", "",
      valRegex(regex.pattern, "Not a valid URL format: must be http/https"))
    
    override def nextScreen = versioning
  }
  
  val versioning = new HelixScreen {
    override def screenName = "Scala Compatibility"
    
    val currentVersion = field("Current Version", "", notNull)
  }
  
  import helix.github.GithubClient
  import helix.github.GithubClient.CurrentContributor
  import net.liftweb.json.JsonAST._
  import helix.domain._
  import helix.db.Storage._
  
  def finish(){
    // fetch the contributors from github
    val contributors = GithubClient.get("/repos/%s/contributors"
      .format(general.sourceURL.is.substring(19))){ json => 
        for {
          JArray(contributors) <- json
          JObject(child) <- contributors
          JField("login", JString(login)) <- child
          JField("avatar_url", JString(avatar)) <- child
          JField("contributions", JInt(contributions)) <- child
        } yield Contributor(login = login,
          avatar = Some(avatar),
          contributions = contributions.toInt
        )
      }
    // add to the db
    createProject(Project(
      name = general.name.is, 
      description = Some(general.description.is), 
      groupId = Some(general.groupId.is), 
      artifactId = Some(general.artifactId.is), 
      sourceURL = Some(general.sourceURL.is),
      addedBy = CurrentContributor.is.map(_.login).toOption,
      contributors = contributors))
  }
}
