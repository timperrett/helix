package helix.sitemap

import helix.domain.Project

case class ProjectDetail(groupId: String, artifactId: String){
  import helix.db.Storage._
  lazy val project: Option[Project] = 
    findProjectByGroupAndArtifact(groupId, artifactId)
}

import scala.xml.{NodeSeq,Text}
import net.liftweb.common.{Box,Empty,Full}
import net.liftweb.util.NamedPF
import net.liftweb.http._
import net.liftweb.sitemap.Loc

object ProjectInformation extends Loc[ProjectDetail]{
  val name = "details"
  
  private val path = "project" :: "show" :: Nil
  
  val text = new Loc.LinkText[ProjectDetail](detail =>
    Text((for(p <- detail.project) yield p.name).getOrElse("Unknown")))
  
  val link = new Loc.Link[ProjectDetail](path, false)
  
  import net.liftweb.sitemap.Loc.Unless
  
  // loc params
  def params = Nil
  /*  Unless(
    () => S.param("groupId").isEmpty,
    () => RedirectResponse("/"))*/
    
  def defaultValue = Empty
  
  override val rewrite: LocRewrite = Full(NamedPF("Project Rewrite"){
    case RewriteRequest(ParsePath("projects" :: gid :: aid :: Nil,"",true,_),_,_) =>
        (RewriteResponse(path), ProjectDetail(gid,aid))
  })
  
  /** snippets **/
  import net.liftweb.util.Helpers._
  
  override val snippets: SnippetTest = {
    case ("information", Full(pd)) => information(pd)
    case ("contributors", Full(pd)) => contributors(pd.project)
  }
  
  def contributors(project: Option[Project]): NodeSeq => NodeSeq = 
    project.map { p => 
      "li" #> p.contributors.map { c =>
        "h4 *" #> c.login &
        "p *" #> "%s commits".format(c.contributions) & 
        "img [src]" #> c.avatar
      }
    } getOrElse "*" #> NodeSeq.Empty

  
  def information(details: ProjectDetail) = 
    (for(project <- details.project)
      yield "h2 *" #> project.name) getOrElse suggestAddingProject
  
  def suggestAddingProject = 
    "*" #> <p>That project does not exist, <a href="/projects/add">add it?</a></p>
  
}
