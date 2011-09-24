package helix.http.ui

import scala.xml.{NodeSeq,Text}
import net.liftweb._, 
  common.{Box,Empty,Full},
  util.Helpers._,
  http.{SHtml,S},
  wizard.Wizard
import helix.domain.Service._
import helix.domain._

object VersionWizard extends Wizard with CommonScreens {
  override def allTemplatePath = "templates-hidden" :: "wizard-modal" :: Nil
  override def finishButton = <button>Add</button>
  
  val versioning = new AddProjectVersionScreen {
    override def screenName = "Add Version"
    override def screenTop = Full(<h3>{screenNameAsHtml}</h3>)
  }
  
  import net.liftweb.http.js.JE.Call
  
  override def calcAjaxOnDone = 
    Call("prependNewVersionRow", 
      versioning.currentVersion.is,
      versioning.versions.is.filter(_._2 == true).map(_._1).mkString(", ")).cmd
  
  def finish(){
    // add version to the database
  }
}
