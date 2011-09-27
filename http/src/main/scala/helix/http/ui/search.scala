package helix.http.ui

import helix.http.ui.DomainBindings._
import helix.domain.Project

object Search extends Snippet with ProjectLists {
  def render = {
    // get the list of projects
    val results: List[Project] = Nil
    // render that project list
    bind(results)
  }
}
