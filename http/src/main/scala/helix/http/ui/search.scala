package helix.http.ui

import net.liftweb.http.S
import helix.http.ui.DomainBindings._
import helix.domain.{Project,Service}

object Search extends Snippet with ProjectLists {
  def render = {
    // get the list of projects
    val results: List[Project] = 
      S.param("q").map { term => 
        Service.search(term)
      } openOr Nil
    // render that project list
    bind(results)
  }
}
