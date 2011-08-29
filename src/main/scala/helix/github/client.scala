package helix.github

import net.liftweb.common.{Box,Full,Empty}
import net.liftweb.util.{NamedPF,Props}
import net.liftweb.http.{Req,GetRequest,LiftRules,
  RedirectResponse,PlainTextResponse,S}

trait Dispatcher extends LiftRules.DispatchPF {
  def dispatch: List[LiftRules.DispatchPF] = Nil
  def isDefinedAt(r: Req) = NamedPF.isDefinedAt(r, dispatch)
  def apply(r: Req) = NamedPF(r, dispatch)
}

import dispatch._
import dispatch.json.Js._
import dispatch.liftjson.Js._
import net.liftweb.util.Helpers
import net.liftweb.http.SessionVar
import net.liftweb.json.JsonAST._
import helix.domain.Contributor

object LoginRedirect extends SessionVar[Box[String]](Empty){
  override def __nameSalt = Helpers.nextFuncName
}

object GithubClient extends Dispatcher {
  import helix.db.Storage._
  
  object AccessToken extends SessionVar[Box[String]](Empty)
  object CurrentContributor extends SessionVar[Box[Contributor]](Empty)
  
  private val tokenCookie = "acstkn"
  private val clientId = Props.get("github.clientid").openOr("unknown")
  private val clientSecret = Props.get("github.secret").openOr("unknown")
  
  def get[T](path: String, params: Map[String,String] = 
      Map("access_token" -> AccessToken.is.openOr("unknown")))(f: JValue => T) = {
    val http = new Http
    val req = url("https://api.github.com" + path) <<? params
    http(req ># f)
  }
  
  def contributor: Box[Contributor] = {
    get("/user"){ json => Box(for { 
        JObject(child) <- json
        JField("name", JString(name)) <- child
        JField("login", JString(login)) <- child
        JField("type", JString(style)) <- child
        JField("avatar_url", JString(url)) <- child
      } yield Contributor(
          login = login, name = Some(name), 
          avatar = Some(url), style = style)
      )
    }
  }
  
  // import net.liftweb.util.Helpers.{urlEncode,urlDecode}
  
  // private def redirectTo = "http://%s:%s/oauth/callback?redirect_to=%s".format(
  //   S.hostName,S.containerRequest.map(_.serverPort).openOr(80),
  //   urlEncode(S.referer.openOr("/")))
  
  override def dispatch = {
    import net.liftweb.http.S
    import net.liftweb.http.provider.HTTPCookie
    
    val login: LiftRules.DispatchPF = NamedPF("Send to Github"){
      case Req("oauth" :: "login" :: Nil, "", GetRequest) => () => Full(
        RedirectResponse(
          "https://github.com/login/oauth/authorize?client_id=%s".format(clientId)))
    }
    
    val token: LiftRules.DispatchPF = NamedPF("Token Handler"){
      case r@Req("oauth" :: "callback" :: Nil, "", GetRequest) => () =>
        for {
          code <- r.param("code")
        } yield {
          val endpoint = "https://github.com/login/oauth/access_token"
          val http = new Http
          val req = url(endpoint) << ("client_id=%s&client_secret=%s&code=%s".format(clientId,clientSecret,code),"application/x-www-form-urlencoded") // 
          // FIXME: This could explode                                                                                                                           d
          val response = http(req.secure as_str)
          // parse the token response
          val TokenResponse = "access_token=(.+)&token_type=(.+)".r
          val TokenResponse(token,typez) = response
          // set the token into a cookie for later
          // and also into the session for this session
          AccessToken(Full(token))
          // set their contributor instance into the session as it'll 
          // be needed later for making API calls
          contributor.foreach(c => CurrentContributor(Full(c)))
          RedirectResponse(LoginRedirect.is.openOr("/")) //, HTTPCookie(tokenCookie, token))
        }
    }
    super.dispatch ::: List(login, token)
  }
}
