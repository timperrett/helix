package helix.http.ui

import scala.xml.NodeSeq
import net.liftweb.util.Helpers._
import net.liftweb.http.DispatchSnippet
import helix.http.Vars.CurrentContributor

object CurrentContributorInfo extends Snippet {
  def render = CurrentContributor.is.map { c =>
    "a *" #> c.login &
    "a [href]" #> c.url &
    "img [src]" #> c.avatar    
  } getOrElse "*" #> NodeSeq.Empty
}

import net.liftweb.http.S

object LoginLink extends Snippet {
  def render = 
    "a [href]" #> "/oauth/login?return_to=%s".format(
      urlEncode(S.attr("return_to").openOr(S.uri)))
}
