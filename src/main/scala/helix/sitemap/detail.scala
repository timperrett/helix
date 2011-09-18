package helix.sitemap

import helix.domain.{Project,ScalaVersion}

case class ProjectDetail(groupId: String, artifactId: String){
  import helix.domain.Service._
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
  import helix.util.DomainBindings._
  
  override val snippets: SnippetTest = {
    case ("information", Full(pd)) => information(pd)
    case ("contributors", Full(pd)) => contributors(pd.project)
    case ("overview", Full(pd)) => overview(pd.project)
    case ("versions", Full(pd)) => versions(pd.project.map(_.versionsDecoded).getOrElse(Map.empty))
  }
  
  def contributors(project: Option[Project]): NodeSeq => NodeSeq = 
    project.map { p => 
      "li" #> p.contributors.map { c =>
        "h4 *" #> c.login &
        "p *" #> "%s commits".format(c.contributions) & 
        "img [src]" #> c.avatar
      }
    } getOrElse "*" #> NodeSeq.Empty
  
  def versions(versions: Map[String, String]) = {
    "tr" #> versions.map { case (version, scalaversion) =>
      "version" #> version &
      "scalaversion" #> scalaversion
    }
  }
  
  def overview(project: Option[Project]): NodeSeq => NodeSeq = {
    import org.joda.time.{Duration,DateTime}
    import org.joda.time.format.PeriodFormatterBuilder
    
    val periodFormatter = new PeriodFormatterBuilder()
        .appendDays.appendSuffix(" day", " days")
        .toFormatter
    
    project.map { p => 
      "latest_version" #> p.versionsDecoded.head._1 & 
      "age" #> periodFormatter.print(new Duration(
        new DateTime(p.createdAt), 
        new DateTime(now)).toPeriod().normalizedStandard) &
      "contributor_count" #> p.contributors.size
    } getOrElse "*" #> NodeSeq.Empty
  }
  
  def information(details: ProjectDetail) = 
    (for(project <- details.project)
      yield project.bind) getOrElse suggestAddingProject
  
  def suggestAddingProject = 
    "*" #> <lift:embed what="_nonexistant_project" />
  
}
