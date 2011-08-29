package helix.snippet

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
  def render = ".prj" #> (for {
    project <- listFiveNewestProjects
    group <- project.groupId
    artifact <- project.artifactId
  } yield {
    ".project-name *" #> project.name &
    ".project-name [href]" #> "/projects/%s/%s".format(group,artifact) &
    "img [src]" #> project.randomContributor.map(_.picture).get &
    "p *" #> project.headline &
    ".tags *" #> project.tags.map { tag => 
      "a [href]" #> "/tags/%s".format(tag.name) &
      "a *" #> tag.name
    }
  })
}