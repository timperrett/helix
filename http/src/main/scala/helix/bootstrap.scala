package bootstrap.liftweb

import scala.xml.{Text,Node}
import net.liftweb.common.{Box,Full,Empty,LazyLoggable}
import net.liftweb.util.{NamedPF,Props}
import net.liftweb.http._
import net.liftweb.sitemap._
import akka.actor.Actor.actorOf
import helix.domain.Service
import helix.http.Vars.AccessToken

class Boot extends LazyLoggable {
  def isAuthenticated: Boolean = !AccessToken.is.isEmpty
  def boot {
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    
    LiftRules.dispatch.append(helix.http.OAuth)
    
    LiftRules.loggedInTest = Full(() => isAuthenticated)
    
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))
    
    LiftRules.uriNotFound.prepend(NamedPF("404handler"){
      case (req,failure) => NotFoundAsTemplate(ParsePath(List("404"),"html",false,false))
    })
    
    // start the actors
    helix.async.Manager.start()
    // stop the actors (on shutdown)
    LiftRules.unloadHooks.append(() => helix.async.Manager.stop())
    
    // LiftRules.exceptionHandler.append {
    //   case (_, r, e) => 
    //     logger.error("Exception being returned to browser when processing " + r.uri.toString + ": " + e.getMessage)
    //     XhtmlResponse(S.runTemplate("500" :: Nil).openOr(<h1>Unexpected Error</h1>), 
    //       S.htmlProperties.docType, 
    //       List("Content-Type" -> "text/html; charset=utf-8"), 
    //       Nil, 500, S.ieMode)
    // }
    
    LiftRules.snippetDispatch.append {
      case "project_wizard" => helix.http.ui.ProjectWizard
      case "version_wizard" => helix.http.ui.VersionWizard
      case "recently_added_projects" => helix.http.ui.RecentlyAddedProject
      case "all_projects" => helix.http.ui.ListAllProjects
      case "most_active_projects" => helix.http.ui.ListMostActiveProjects
      case "contributor_info" => helix.http.ui.CurrentContributorInfo
      case "login_link" => helix.http.ui.LoginLink
      case "statistics" => helix.http.ui.ProjectStatistics
    }
    
    LiftRules.statelessTest.append {
      case "search" :: Nil => true
    }
    
    /**
     * Sitemap setup
     */
    import net.liftweb.sitemap.Loc.If
    import helix.http.ui.ProjectInformation
    
    LiftRules.setSiteMap(SiteMap(
      Menu("Home") / "index",
      Menu("Error") / "error",
      Menu("About") / "about",
      Menu("Search") / "search",
      // Menu("Tags: List") / "tags",
      Menu("Projects: List") / "projects",
      Menu("Projects: Add") / "project" / "add" >> If(
        () => isAuthenticated, 
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
