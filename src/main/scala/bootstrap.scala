package bootstrap.liftweb

import net.liftweb.common.{Box,Full,Empty}
import net.liftweb.http.{LiftRules,RewriteRequest,Req,
  Html5Properties,RewriteResponse,ParsePath,DocType}
import net.liftweb.sitemap._
import scalastack.github.GithubClient
import scalastack.github.GithubClient.AccessToken

class Boot {
  def boot {
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    LiftRules.dispatch.append(GithubClient)

    LiftRules.loggedInTest = Full(() => !AccessToken.isEmpty)
    
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))
    
    // LiftRules.docType.default.set((r: Req) => Full(DocType.html5))
    
    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath("projects" :: link :: Nil,"",true,_),_,_) =>
           RewriteResponse("project" :: Nil, Map("project" -> link))
    }
    
    LiftRules.setSiteMap(SiteMap(
      Menu("Home") / "index"
    ))
  }
}
