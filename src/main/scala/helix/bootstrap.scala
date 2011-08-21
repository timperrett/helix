package bootstrap.liftweb

import net.liftweb.common.{Box,Full,Empty}
import net.liftweb.http.{LiftRules,RewriteRequest,Req,RedirectResponse,
  Html5Properties,RewriteResponse,ParsePath,DocType}
import net.liftweb.sitemap._
import helix.github.GithubClient
import helix.github.GithubClient.AccessToken

class Boot {
  def boot {
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    LiftRules.dispatch.append(GithubClient)

    LiftRules.loggedInTest = Full(() => !AccessToken.isEmpty)
    
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))
    
    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath("projects" :: link :: Nil,"",true,_),_,_) =>
           RewriteResponse("project" :: "detail" :: Nil, Map("project" -> link))
    }
    
    LiftRules.snippetDispatch.append {
      case "projects_recently_added" => helix.snippet.MostRecentlyAdded
      case "projects_detail" => helix.snippet.Detail
      case "contributor_info" => helix.snippet.CurrentContributorInfo
    }
    
    import net.liftweb.sitemap.Loc.Unless
    
    LiftRules.setSiteMap(SiteMap(
      Menu("Home") / "index",
      Menu("Projects: Add") / "project" / "add" >> Unless(
        () => AccessToken.isEmpty, () => RedirectResponse("/oauth/login")),
      Menu("Projects: Detail") / "project" / "detail"
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
