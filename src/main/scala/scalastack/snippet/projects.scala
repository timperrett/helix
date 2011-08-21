package scalastack.snippet

import net.liftweb.common.Box
import net.liftweb.util.Helpers._
import net.liftweb.http.S
import scalastack.db.Storage._

class Projects {
  def newest = listNewestAdded map { project => 
    "*" #> "dfsdf"
  }
  // def mostRecent = 
  
  def detail = S.param("project") flatMap { link => 
    Box(findProjectByPermalink(link)) map { proj =>
      ".name" #> proj.name
    }
  } openOr <p>You didnt specify a project fool!</p>
  
}