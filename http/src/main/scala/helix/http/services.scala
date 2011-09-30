package helix.http

import net.liftweb.common.{Box,Full,Empty}
import net.liftweb.util.{NamedPF,Helpers}
import net.liftweb.http._
import helix.domain.{Project,Service}, Service._

object ProjectServices extends Dispatcher {
  import net.liftweb.json._
  
  // god aweful work around to the issue between
  // salat and lift-json. The former expects a single
  // constructor ONLY, whilst lift-json can handle
  // multiple ones. Having both however causes a meltdown.
  private case class WireProject(
    name: String, headline: String,
    description: String, sourceURL: String, 
    repositoryURL: String){
      def toProject = Project(
          name = name, headline = Some(headline),
          description = Some(description), sourceURL = Some(sourceURL),
          repositoryURL = Some(repositoryURL)
        )
    }
  
   /**
   {
     "name": "Lift",
     "headline": "Lift is an expressive web framework",
     "description": "blah blah blah blah",
     "sourceURL": "http://github.com/lift/framework",
     "repositoryURL": "http://scala-tools.org/repo-releases/"
   }
    */
  private implicit val formats = DefaultFormats

  private def given(request: Req, group: String, artifact: String)(f: Project => LiftResponse): LiftResponse = 
    (for {
      j <- request.json
      p <- Helpers.tryo(j.extract[WireProject].toProject)
    } yield {
       f(p.copy(groupId = Some(group), artifactId = Some(artifact)))
     }) openOr BadResponse()
   
  
  override def dispatch = {
    /*
    GET /project/com.twitter/gizzard - Displays the project via the UI
    PUT /project/com.twitter/gizzard - Would update the existing project or create a new one
    POST /project/com.twitter/gizzard/0.1.2 - Add a new project version (append only)
    */
    val feed: LiftRules.DispatchPF = NamedPF("Helix Services"){
      // add/update project
      case request@Req("project" :: group :: artifact :: Nil, "", PutRequest) => () => 
        Box !! (given(request, group, artifact){ project => 
          save(project){ pr =>
            asyncronuslyUpdate(pr)
          }.map(_ => OkResponse()).getOrElse(InternalServerErrorResponse())
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