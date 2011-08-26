package bootstrap.liftweb

import net.liftweb.common.{Box,Full,Empty}
import net.liftweb.http.{LiftRules,RewriteRequest,Req,RedirectResponse,
  Html5Properties,RewriteResponse,ParsePath,S}
import net.liftweb.sitemap._

import helix.github.GithubClient
import helix.github.GithubClient.AccessToken
import helix.db.Storage

class Boot {
  def boot {
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    LiftRules.dispatch.append(GithubClient)

    LiftRules.loggedInTest = Full(() => !AccessToken.isEmpty)
    
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))
    
    LiftRules.statelessRewrite.append {
      // e.g. /projects/net.liftweb/lift-webkit/2.3
      case RewriteRequest(ParsePath("projects" :: gid :: aid :: Nil,"",true,_),_,_) =>
           RewriteResponse("project" :: "show" :: Nil, 
             Map("groupId" -> gid, "artifactId" -> aid))
    }
    
    LiftRules.snippetDispatch.append {
      case "project_wizard" => helix.snippet.ProjectWizard
      case "add_new_project" => helix.snippet.AddProjectForm
      case "recently_added_projects" => helix.snippet.RecentlyAddedProject
      case "project_details" => helix.snippet.ProjectDetails
      case "contributor_info" => helix.snippet.CurrentContributorInfo
    }
    
    import net.liftweb.sitemap.Loc.Unless
    
    LiftRules.setSiteMap(SiteMap(
      Menu("Home") / "index",
      Menu("Tags: List") / "tags",
      Menu("Projects: List") / "projects",
      Menu("Projects: Add") / "project" / "add" >> Unless(
        () => AccessToken.isEmpty, 
        () => RedirectResponse("/oauth/login")),
      Menu("Projects: Detail") / "project" / "show" >> Unless(
        () => S.param("groupId").isEmpty,
        () => RedirectResponse("/"))
    ))
  }
}


import javax.servlet._ 
import javax.servlet.http._ 
import net.liftweb.http.LiftFilter 

class CloudBeesLiftFilter extends LiftFilter { 
  private def run_mode_set_? = (null != System.getProperty("run.mode"))
  private def run_mode_from(config: FilterConfig) = Option(config.getServletContext.getInitParameter("run.mode"))
  override def init(config: FilterConfig) { 
    if (!run_mode_set_?) {
      val mode = run_mode_from(config) getOrElse "development"
      System.setProperty("run.mode", mode)
    }
    super.init(config) 
  }
}
