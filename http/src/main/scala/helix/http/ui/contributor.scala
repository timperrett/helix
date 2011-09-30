package helix.http.ui

import helix.domain.{Service,Contributor}

case class ContributorDetail(login: String){
  lazy val contributor: Option[Contributor] = 
    Service.findContributorByLogin(login)
}

import scala.xml.{NodeSeq,Text}
import net.liftweb.common.{Box,Empty,Full}
import net.liftweb.util.NamedPF
import net.liftweb.util.Helpers._
import net.liftweb.http._
import net.liftweb.sitemap.Loc
import helix.http.ui.DomainBindings._

object ContributorInformation extends Loc[ContributorDetail]{
  val name = "contributor details"
  
  private val path = "contributor" :: "show" :: Nil
  
  val text = new Loc.LinkText[ContributorDetail](detail =>
    Text((for(c <- detail.contributor; n <- c.name) yield n).getOrElse("Unknown")))
  
  val link = new Loc.Link[ContributorDetail](path, false)
  
  def params = Nil
  // import net.liftweb.sitemap.Loc.{If,EarlyResponse}
  // def params = EarlyResponse(() => {
  //   println(">>>>>>>>>>>>>>")
  //   println(currentValue)
  //   Empty
  // }) :: Nil
    
  def defaultValue = Empty
  
  override val rewrite: LocRewrite = Full(NamedPF("Contributor Rewrite"){
    case RewriteRequest(ParsePath("contributors" :: login :: Nil,"",true,_),_,_) =>
        (RewriteResponse(path), ContributorDetail(login))
  })
  
  override val snippets: SnippetTest = {
    case ("information", Full(cd)) => information(cd)
  }
  
  val dontDisplayAnything = "*" #> NodeSeq.Empty
  
  def information(details: ContributorDetail) = 
    (for(contributor <- details.contributor) 
      yield contributor.bind) getOrElse dontDisplayAnything
  
}