package helix.http

import net.liftweb.common.{Box,Full,Empty}
import net.liftweb.util.{NamedPF,Helpers}
import net.liftweb.http.{Req,LiftRules,LiftResponse,RedirectResponse}

trait Dispatcher extends LiftRules.DispatchPF {
  def dispatch: List[LiftRules.DispatchPF] = Nil
  def isDefinedAt(r: Req) = NamedPF.isDefinedAt(r, dispatch)
  def apply(r: Req) = NamedPF(r, dispatch)
}

object OAuth extends Dispatcher {
  import helix.domain.Service._
  import helix.util.Config._
  import helix.http.Vars._
  
  private val clientId = Conf.get[String]("github.clientid").getOrElse("unknown")
  private val clientSecret = Conf.get[String]("github.secret").getOrElse("unknown")
  
  private val callbackCodeHandler: Req => Box[LiftResponse] = r => 
    for(code <- r.param("code")) yield {
      // set the token into the session 
      AccessToken(github.requestAccessToken(clientId, clientSecret, code))
      // set their contributor instance into the session as it'll 
      // be needed later for making API calls
      for {
        token <- AccessToken.is
      } CurrentContributor(github.contributor(token))
      
      RedirectResponse(r.param("return_to").map(Helpers.urlDecode).openOr("/"))
    }
  
  private val callbackErrorHandler: Req => Box[LiftResponse] = r => 
    for(e <- r.param("error")) yield RedirectResponse("/500?because=%s".format(e))
  
  override def dispatch = {
    import net.liftweb.http.{S,GetRequest}
    import net.liftweb.http.provider.HTTPCookie
    
    val login: LiftRules.DispatchPF = NamedPF("Send to Github"){
      case r@Req("oauth" :: "login" :: Nil, "", GetRequest) => () => {
        val returnURL = S.param("return_to").map(Helpers.urlDecode).openOr("/")
        val callbackURL = "%s/oauth/callback?return_to=%s".format(r.hostAndPath,returnURL)
        Full((for(c <- CurrentContributor)
          yield RedirectResponse(returnURL)) getOrElse RedirectResponse {
            "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s"
              .format(clientId, Helpers.urlEncode(callbackURL))
          } 
        )
      }
    }
    
    val token: LiftRules.DispatchPF = NamedPF("Token Handler"){
      case r@Req("oauth" :: "callback" :: Nil, "", GetRequest) => () =>
        callbackCodeHandler(r) or callbackErrorHandler(r)
    }
    super.dispatch ::: List(login, token)
  }
}
