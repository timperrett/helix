package helix.http

import net.liftweb.common.{Box,Full,Empty}
import net.liftweb.util.{NamedPF,Helpers}
import net.liftweb.http._
import helix.domain.{Project,Service}, Service._

// object SearchServices extends Dispatcher with helix.search.ProjectSearching with helix.search.ElasticSearchProvider {
//   import akka.actor.Actor.registry
//   
//   override def dispatch = {
//     val srch: LiftRules.DispatchPF = NamedPF("Helix Search"){
//       case Req("searchx" :: Nil, "", GetRequest) => () => 
//         for(term <- S.param("q")
//           ) yield PlainTextResponse(search(term).map(_.id).mkString(", "))
//     }
//     super.dispatch ::: List(srch)
//   }
// }

object PublishingServices extends Dispatcher {
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
    POST /project/com.twitter/gizzard- Add a new project version (append only)
    */
    val feed: LiftRules.DispatchPF = NamedPF("Helix Publishing"){
      /**
       {
         "name": "Lift",
         "headline": "Lift is an expressive web framework",
         "description": "blah blah blah blah",
         "sourceURL": "http://github.com/lift/framework",
         "repositoryURL": "http://scala-tools.org/repo-releases/"
       }
        */
      case request@Req("project" :: group :: artifact :: Nil, "", PutRequest) => () => 
        Box !! (given(request, group, artifact){ project => 
          save(project){ pr =>
            asyncronuslyUpdate(pr)
          }.map(_ => OkResponse()).getOrElse(InternalServerErrorResponse())
        })
      case Req("project" :: group :: artifact :: Nil, "", 
        GetRequest | DeleteRequest) => () => Full(MethodNotAllowedResponse())
      
        /**
        {
          "versions": [
            {
              "identifier": "0.1.0-SNAPSHOT",
              "description": "blah blah blah blah",
              "scalaversions": [ "2.9.1", "2.8.1" ]
            }
          ]
        }
        */
      // can only be used for modifying project versions and modules
      case Req("project" :: group :: artifact :: Nil, "", PostRequest) => () => {
          // given(request, group, artifact){ project => 
          //   if(!project.hasVersion(version)){
          //     // create version
          //     project.copy()
          //   } else {
          //     // bad request
          //     BadResponse()
          //   }
          // }
          
          
          // does the version exist already?
          // if yes, update, else insert
          Empty
      }
    }
    super.dispatch ::: List(feed)
  }
}