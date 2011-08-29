package helix.snippet

import scala.xml.NodeSeq
import net.liftweb.common.Box
import net.liftweb.util.Helpers._
import net.liftweb.http.{S,SHtml,DispatchSnippet}
import helix.db.Storage._
import helix.domain.Project
import helix.github.Client.CurrentContributor


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
    "p *" #> project.description &
    ".tags *" #> project.tags.map { tag => 
      "a [href]" #> "/tags/%s".format(tag.name) &
      "a *" #> tag.name
    }
  })
}

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

