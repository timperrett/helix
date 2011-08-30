package helix.snippet

import scala.xml.NodeSeq
import net.liftweb.util.Helpers._
import net.liftweb.http.{S,SHtml,DispatchSnippet}
import helix.db.Storage._

/**
 * List the top 5 recently added projects
 */
object RecentlyAddedProject extends DispatchSnippet {
  def dispatch = {
    case _ => render
  }
  import helix.util.DomainBindings._
  
  def render = ".prj" #> 
    (for(project <- listFiveNewestProjects)
      yield project.bind)
}
