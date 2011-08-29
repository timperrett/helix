package helix.snippet

import scala.xml.NodeSeq
import net.liftweb.util.Helpers._
import net.liftweb.http.DispatchSnippet
import helix.github.Client.CurrentContributor

object CurrentContributorInfo extends DispatchSnippet {
  def dispatch = {
    case _ => render
  }
  def render = CurrentContributor.is.map { c =>
    "a *" #> c.login &
    "a [href]" #> "http://github.com/%s".format(c.login) &
    "img [src]" #> c.avatar    
  } openOr "*" #> NodeSeq.Empty
}
