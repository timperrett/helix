package helix.http.ui

import scala.xml.{NodeSeq,Text}
import net.liftweb._, common.{Box,Empty,Full}, 
  util.CssSel, util.Helpers._, 
  http.{SHtml,S,DispatchSnippet}, wizard.Wizard
import helix.domain.Service._
import helix.domain._

trait Snippet extends DispatchSnippet {
  def dispatch = {
    case _ => render
  }
  def render: CssSel
}
