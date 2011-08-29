package helix.snippet

import net.liftweb.util.Helpers._
import net.liftweb.http.{S,SHtml,DispatchSnippet}
import helix.db.Storage._

/** 
 * Display the full set of project information
 */
object ProjectDetails extends DispatchSnippet {
  def dispatch = {
    case _ => render
  }
  def render = (for {
    group <- S.param("groupId")
    artifact <- S.param("artifactId")
    project <- findProjectByGroupAndArtifact(group,artifact)
  } yield {
    "h2 *" #> project.name
  }) openOr "*" #> <p>You didnt specify a project fool!</p>
}

