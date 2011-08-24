package helix.snippet

import scala.xml.NodeSeq
import net.liftweb.common.Box
import net.liftweb.util.Helpers._
import net.liftweb.http.{S,SHtml,DispatchSnippet}
import helix.db.Storage._
import helix.domain.Project
import helix.github.GithubClient.CurrentContributor

object AddProjectForm extends DispatchSnippet {
  def dispatch = {
    case _ => render
  }
  def render = {
    var name, description, groupId, artifactId, version, sourceURL = ""
    "@sourceurl" #> SHtml.text(sourceURL, sourceURL = _) &
    "@name" #> SHtml.text(name, name = _) &
    "@groupid" #> SHtml.text(groupId, groupId = _) &
    "@artifactid" #> SHtml.text(artifactId, artifactId = _) &
    "@version" #> SHtml.text(version, version = _) &
    "@description" #> SHtml.textarea(description, description = _) &
    "button" #> SHtml.onSubmitUnit(() => {
      if(createProject(
        Project(name = name, description = Some(description), 
          groupId = Some(groupId), artifactId = Some(artifactId), version = version,
          sourceURL = Some(sourceURL),
          addedBy = CurrentContributor.is.map(_.login).toOption
        )
      )) S.redirectTo("/projects/%s/%s".format(groupId,artifactId))
      else S.error("Unable to add project. Please try again.")
    })
  }
}

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
    ".project-name [href]" #> "/projects/%s/%s".format(group,artifact)
  })
}

object ProjectDetails extends DispatchSnippet {
  def dispatch = {
    case _ => render
  }
  def render = "*" #> <p>You didnt specify a project fool!</p>
  
  // S.param("project") flatMap { link => 
  //   Box(findProjectByPermalink(link)) map { proj =>
  //     ".name" #> proj.name
  //   }
  // } openOr "*" #> <p>You didnt specify a project fool!</p>
}

