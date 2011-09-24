package helix.http.ui

import scala.xml.NodeSeq
import net.liftweb.util.Helpers._
import net.liftweb.http.DispatchSnippet
import helix.github.Client.CurrentContributor

object CurrentContributorInfo extends Snippet {
  def render = CurrentContributor.is.map { c =>
    "a *" #> c.login &
    "a [href]" #> "http://github.com/%s".format(c.login) &
    "img [src]" #> c.avatar    
  } openOr "*" #> NodeSeq.Empty
}

import net.liftweb.http.S

object LoginLink extends Snippet {
  def render = 
    "a [href]" #> "/oauth/login?return_to=%s".format(
      urlEncode(S.attr("return_to").openOr(S.uri)))
}
