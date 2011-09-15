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

/**
 * List all projects in the system (paginated)
 */
object ListAllProjects extends Snippet with ProjectLists {
  def render = bind(listProjectsAlphabetically())
}

/**
 * Ask the statistics actor for the latest figures
 * about system and project counts
 */
import akka.actor.ActorRef
import akka.dispatch.Future
import helix.actor.SystemStatistics

class ProjectStatistics(actor: ActorRef) extends Snippet {
  import SystemStatistics._ 
  
  def render = "count" #> (for {
    r <- actor !! GetProjectCount
  } yield r.toString).getOrElse(" a bunch ")
}
