package helix.snippet

import scala.xml.NodeSeq
import net.liftweb.util.Helpers._
import net.liftweb.http.{S,SHtml,DispatchSnippet,PaginatorSnippet}
import helix.domain.Service._
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
object ListAllProjects extends DispatchSnippet with PaginatorSnippet[Project] with ProjectLists {
  def dispatch = {
    case "list" => list
    case "paginate" => paginate _
  }
  // paginator overrides
  override val itemsPerPage = 20
  override val prevXml = xml.Text("Previous")
  override val nextXml = xml.Text("Next")
  override def pageXml(newFirst: Long, ns: NodeSeq): NodeSeq =
    if(first==newFirst || newFirst < 0 || newFirst >= count)
      <li class="active"><a href="#">{ns}</a></li>
    else
      <li><a href={pageUrl(newFirst)}>{ns}</a></li>
  
  def count = totalProjectCount // from global agent
  def page = listProjectsAlphabetically(itemsPerPage, first.toInt)
  def list = bind(page) 
}

object ListMostActiveProjects extends Snippet with ProjectLists {
  def render = bind(listFiveMostActiveProjects)
}

/**
 * Ask the statistics actor for the latest figures
 * about system and project counts
 */
object ProjectStatistics extends Snippet {
  def render = "count" #> totalProjectCount
}
