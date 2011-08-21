package bootstrap.liftweb

import net.liftweb.common.{Box,Full,Empty}
import net.liftweb.http.LiftRules
import net.liftweb.sitemap._
import scalastack.github.GithubClient
import scalastack.github.GithubClient.AccessToken

class Boot {
  def boot {
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    LiftRules.dispatch.append(GithubClient)

    LiftRules.loggedInTest = Full(() => !AccessToken.isEmpty)
    
    LiftRules.setSiteMap(SiteMap(
      Menu("Home") / "index"
    ))
  }
}
