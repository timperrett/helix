package scalastack.github

import net.liftweb.common.{Box,Full,Empty}
import net.liftweb.util.{NamedPF,Props}
import net.liftweb.http.{Req,GetRequest,LiftRules,
  RedirectResponse,PlainTextResponse}

trait Dispatcher extends LiftRules.DispatchPF {
  def dispatch: List[LiftRules.DispatchPF] = Nil
  def isDefinedAt(r: Req) = NamedPF.isDefinedAt(r, dispatch)
  def apply(r: Req) = NamedPF(r, dispatch)
}

import dispatch._
import dispatch.json.Js._
import dispatch.liftjson.Js._
import net.liftweb.http.SessionVar
import net.liftweb.json.JsonAST._
import scalastack.domain.Contributor

object GithubClient extends Dispatcher {
  
  object AccessToken extends SessionVar[Box[String]](Empty)
  
  private val tokenCookie = "acstkn"
  private val clientId = Props.get("github.clientid").openOr("unknown")
  private val clientSecret = Props.get("github.secret").openOr("unknown")

  def get[T](path: String, params: Map[String,String] = Map("access_token" -> AccessToken.is.openOr("unknown")))(f: JValue => T) = {
    val http = new Http
    val req = url("https://api.github.com" + path) <<? params
    http(req ># f)
  }
  
  def contributor: Box[Contributor] = {
    get("/user"){ json => 
      Box(for { 
        JObject(child) <- json
        JField("name", JString(name)) <- child
        JField("login", JString(login)) <- child
        JField("type", JString(style)) <- child
        JField("avatar_url", JString(url)) <- child
      } yield Contributor(name, login, style, url))
    }
  }
  
  override def dispatch = {
    import net.liftweb.http.S
    import net.liftweb.http.provider.HTTPCookie
    import scalastack.db.Storage._
    
    val login: LiftRules.DispatchPF = NamedPF("Send to Github"){
      case Req("oauth" :: "login" :: Nil, "", GetRequest) => () => 
        S.findCookie(tokenCookie) match {
          case Full(token) => Empty
          case _ => Full(RedirectResponse("https://github.com/login/oauth/authorize?client_id=%s&scope=user".format(clientId)))
        }
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
          S.addCookie(HTTPCookie(tokenCookie, token))
          AccessToken(Full(token))


          import org.scalaquery.session._
          import org.scalaquery.session.Database.threadLocalSession
          import org.scalaquery.ql._
          import org.scalaquery.ql.TypeMapper._
          import org.scalaquery.ql.extended.MySQLDriver.Implicit._
          import org.scalaquery.ql.extended.{ExtendedTable => Table}
          
          // check to see if this person is already in
          // the app database, if not, add them
          db.withSession {
            contributor.foreach { c =>
              println(">>>>>>>>>>>")
              println(c)
              findContributorByLogin(c.login) match {
                case Some(id) => println("++++++++++") // do nothing
                case None => Contributors.insert(0, c.name, c.login, c.avatar, c.style)
              }
            }
          }
          
          // send to index
          RedirectResponse("/")
        }
    }
    super.dispatch ::: List(login, token)
  }
}
