package helix.http

import net.liftweb.common.{Box,Full,Empty}
import net.liftweb.util.NamedPF
import net.liftweb.http.{Req,LiftRules,GetRequest,PutRequest,PostRequest}

object Services extends Dispatcher {
  override def dispatch = {
    
    /*
    GET /project/com.twitter/gizzard - Displays the project via the UI
    PUT /project/com.twitter/gizzard - Would update the existing project or create a new one
    POST /project/com.twitter/gizzard/0.1.2 - Add a new project version (append only)
    */
    val feed: LiftRules.DispatchPF = NamedPF("Helix Services"){
      // add/update project
      case Req("project" :: group :: artifact :: Nil, "rss", PutRequest) => () => {
        // does the project exist already?
        // if yes, update, else insert
        Empty
      }
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