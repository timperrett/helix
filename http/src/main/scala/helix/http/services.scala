package helix.http

import net.liftweb.common.{Box,Full,Empty}
import net.liftweb.util.{NamedPF,Helpers}
import net.liftweb.http._
import helix.domain.{Project,Service}, Service._

object Services extends Dispatcher {
  import net.liftweb.json._
  
   /**
   {
     "name": "Lift",
     "headline": "Lift is an expressive web framework",
     "description": "blah blah blah blah",
     "groupId": "net.liftweb",
     "artifactId": "lift-webkit",
     "sourceURL": "http://github.com/lift/framework",
     "repositoryURL": "http://scala-tools.org/repo-releases/"
   }
    */
  private implicit val formats = DefaultFormats
  private def jsonToProject(json: JsonAST.JValue): Box[Project] = 
     Helpers.tryo(json.extract[Project])
   
   private def given(request: Req)(f: Project => LiftResponse): LiftResponse = 
     (for {
       j <- request.json
       p <- jsonToProject(j)
     } yield f(p)) openOr BadResponse()
   
   private def insertOrUpdate(project: Project): Option[Project] = 
     (for {
       group <- project.groupId
       artifact <- project.artifactId
       p <- findProjectByGroupAndArtifact(group, artifact)
     } yield {
       updateProject(project.id, project)
       project
     }) //orElse None //createProject(project)
  
  override def dispatch = {
    /*
    GET /project/com.twitter/gizzard - Displays the project via the UI
    PUT /project/com.twitter/gizzard - Would update the existing project or create a new one
    POST /project/com.twitter/gizzard/0.1.2 - Add a new project version (append only)
    */
    val feed: LiftRules.DispatchPF = NamedPF("Helix Services"){
      // add/update project
      case request@Req("project" :: group :: artifact :: Nil, "rss", PutRequest) => () => 
        Box(given(request){ project => 
          insertOrUpdate(project).map(_ => OkResponse()
            ).getOrElse(InternalServerErrorResponse())
        })
        
      // add/update project version
      case Req("project" :: group :: artifact :: version :: Nil, "", PostRequest) => () => {
        // does the version exist already?
        // if yes, update, else insert
        Empty
      }
    }
    super.dispatch ::: List(feed)
  }
}