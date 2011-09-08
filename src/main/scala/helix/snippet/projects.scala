package helix.snippet

import scala.xml.NodeSeq
import net.liftweb.util.Helpers._
import net.liftweb.http.{S,SHtml,DispatchSnippet}
import helix.db.Storage._
import helix.util.DomainBindings._
import helix.domain.Project

trait ProjectLists {
  protected def bind(projects: List[Project]) = ".prj" #> 
    (for(project <- projects)
      yield project.bind)
}

/**
 * List the top 5 recently added projects
 */
object RecentlyAddedProject extends Snippet with ProjectLists {
  def render = bind(listFiveNewestProjects)
}

object ListAllProjects extends Snippet with ProjectLists {
  def render = bind(listProjectsAlphabetically())
}