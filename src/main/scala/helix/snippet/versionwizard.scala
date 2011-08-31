package helix.snippet

import scala.xml.{NodeSeq,Text}
import net.liftweb._, 
  common.{Box,Empty,Full},
  util.Helpers._,
  http.{SHtml,S},
  wizard.Wizard
import helix.db.Storage._
import helix.domain._

object VersionWizard extends Wizard with CommonScreens {
  override def allTemplatePath = "templates-hidden" :: "wizard-modal" :: Nil
  override def finishButton = <button>Add</button>
  
  val versioning = new AddProjectVersionScreen {
    override def screenName = "Add Version"
    override def screenTop = Full(<h3>{screenNameAsHtml}</h3>)
  }
  
  def finish(){
    println(">>>>>>>>>>> ADDING Version")
    println(versioning.versions.is.foreach(println))
  }
}
