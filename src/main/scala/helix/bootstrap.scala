package bootstrap.liftweb

import scala.xml.{Text,Node}
import net.liftweb.common.{Box,Full,Empty,LazyLoggable}
import net.liftweb.util.{NamedPF,Props}
import net.liftweb.http._
import net.liftweb.sitemap._
import helix.github.{Client => Github}
import helix.domain.Service
import akka.actor.Actor.actorOf

class Boot extends LazyLoggable {
  def boot {
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    
    LiftRules.dispatch.append(helix.github.OAuth)
    
    LiftRules.loggedInTest = Full(() => Github.isAuthenticated)
    
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))
    
    LiftRules.uriNotFound.prepend(NamedPF("404handler"){
      case (req,failure) => NotFoundAsTemplate(ParsePath(List("404"),"html",false,false))
    })
    
    /** 
     * Akka setup and teardown
     */
    import akka.actor.Actor.actorOf
    val dailyRunner = actorOf[helix.async.DaliyRunner]
    dailyRunner.start()
    
    LiftRules.unloadHooks.append(() => {
      helix.async.TotalProjectCount.close()
      akka.actor.Actor.registry.shutdownAll
    })
    
    // LiftRules.exceptionHandler.append {
    //   case (_, r, e) => 
    //     logger.error("Exception being returned to browser when processing " + r.uri.toString + ": " + e.getMessage)
    //     XhtmlResponse(S.runTemplate("500" :: Nil).openOr(<h1>Unexpected Error</h1>), 
    //       S.htmlProperties.docType, 
    //       List("Content-Type" -> "text/html; charset=utf-8"), 
    //       Nil, 500, S.ieMode)
    // }
    
    LiftRules.snippetDispatch.append {
      case "project_wizard" => helix.snippet.ProjectWizard
      case "version_wizard" => helix.snippet.VersionWizard
      case "recently_added_projects" => helix.snippet.RecentlyAddedProject
      case "all_projects" => helix.snippet.ListAllProjects
      case "most_active_projects" => helix.snippet.ListMostActiveProjects
      case "contributor_info" => helix.snippet.CurrentContributorInfo
      case "login_link" => helix.snippet.LoginLink
      case "statistics" => helix.snippet.ProjectStatistics
    }
    
    /**
     * Sitemap setup
     */
    import net.liftweb.sitemap.Loc.{Unless,If}
    import helix.sitemap.ProjectInformation
    
    LiftRules.setSiteMap(SiteMap(
      Menu("Home") / "index",
      Menu("Error") / "error",
      Menu("Tags: List") / "tags",
      Menu("Projects: List") / "projects",
      Menu("Projects: Add") / "project" / "add" >> Unless(
        () => !Github.isAuthenticated, 
        () => RedirectResponse("/oauth/login?return_to=%2Fproject%2Fadd")),
      Menu(ProjectInformation)
    ))
  }
}

import javax.servlet._ 
import javax.servlet.http._ 
import net.liftweb.http.LiftFilter 

class CloudBeesLiftFilter extends LiftFilter { 
  private def hasRunMode = (null != System.getProperty("run.mode"))
  private def runModeFrom(config: FilterConfig) = Option(config.getServletContext.getInitParameter("run.mode"))
  override def init(config: FilterConfig) { 
    if (!hasRunMode) {
      val mode = runModeFrom(config) getOrElse "development"
      System.setProperty("run.mode", mode)
    }
    super.init(config) 
  }
}
