package helix.snippet

import scala.xml.NodeSeq
import net.liftweb.common.Box
import net.liftweb.util.Helpers._
import net.liftweb.http.{S,DispatchSnippet}
import helix.db.Storage._

object MostRecentlyAdded extends DispatchSnippet {
  def dispatch = {
    case _ => render
  }
  def render = ".prj" #> listNewestAdded.map { project =>
    ".project-name *" #> project.name &
    ".project-name [href]" #> project.permalink.map("/projects/%s".format(_)).getOrElse("/") & 
    "p *" #> project.description &
    ".tags *" #> project.tags.map { tag => 
      "a [href]" #> "/tags/%s".format(tag.name) &
      "a *" #> tag.name
    }
  } 
}

object Detail extends DispatchSnippet {
  def dispatch = {
    case _ => render
  }
  def render = S.param("project") flatMap { link => 
    Box(findProjectByPermalink(link)) map { proj =>
      ".name" #> proj.name
    }
  } openOr "*" #> <p>You didnt specify a project fool!</p>
}

