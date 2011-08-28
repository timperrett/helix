package helix.snippet

import scala.xml.NodeSeq
import net.liftweb.common.Box
import net.liftweb.util.Helpers._
import net.liftweb.http.{S,SHtml,DispatchSnippet}
import helix.db.Storage._
import helix.domain.Project
import helix.github.GithubClient.CurrentContributor

/** 
 * Add a new project to the helix directory
 */
// object AddProjectForm extends DispatchSnippet {
//   def dispatch = {
//     case _ => render
//   }
//   def render = {
//     var name, description, groupId, artifactId, version, sourceURL = ""
//     "@sourceurl" #> SHtml.text(sourceURL, sourceURL = _) &
//     "@name" #> SHtml.text(name, name = _) &
//     "@groupid" #> SHtml.text(groupId, groupId = _) &
//     "@artifactid" #> SHtml.text(artifactId, artifactId = _) &
//     // "@version" #> SHtml.text(version, version = _) &
//     "@description" #> SHtml.textarea(description, description = _) &
//     "button" #> SHtml.onSubmitUnit(() => {
//       if(createProject(
//         Project(name = name, description = Some(description), 
//           groupId = Some(groupId), artifactId = Some(artifactId), version = version,
//           sourceURL = Some(sourceURL),
//           addedBy = CurrentContributor.is.map(_.login).toOption
//         )
//       )) S.redirectTo("/projects/%s/%s".format(groupId,artifactId))
//       else S.error("Unable to add project. Please try again.")
//     })
//   }
// }

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

