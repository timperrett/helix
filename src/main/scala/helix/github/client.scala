package helix.github

object Client {
  import net.liftweb.http.SessionVar
  import net.liftweb.common.{Box,Full,Empty}
  import helix.domain.Contributor
  import net.liftweb.util.Helpers
  
  object AccessToken extends SessionVar[Box[String]](Empty)
  object CurrentContributor extends SessionVar[Box[Contributor]](Empty)
  
  def isAuthenticated: Boolean = 
    !AccessToken.is.isEmpty
  
  import dispatch._
  import dispatch.json.Js._
  import dispatch.liftjson.Js._
  import net.liftweb.json.JsonAST._
  
  def get[T](path: String, params: Map[String,String] = 
      AccessToken.is.map(t => Map("access_token" -> t)).openOr(Map.empty))(f: JValue => T) = {
    val http = new Http
    val req = url("https://api.github.com" + path) <<? params
    var resp = http(req ># f)
    http.shutdown()
    resp
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
  
  def contributorsFor(repo: String) = 
    get("/repos/%s/contributors".format(repo)){ json => 
      for {
        JArray(contributors) <- json
        JObject(child) <- contributors
        JField("login", JString(login)) <- child
        JField("avatar_url", JString(avatar)) <- child
        JField("contributions", JInt(contributions)) <- child
      } yield Contributor(login = login,
        avatar = Some(avatar),
        contributions = contributions.toInt
      )
    }
  
  
  // FIXME: This is FUGLY. 
  def requestAccessToken(clientId: String, clientSecret: String, code: String): Box[String] = {
    val endpoint = "https://github.com/login/oauth/access_token"
    val http = new Http
    val req = url(endpoint) << ("client_id=%s&client_secret=%s&code=%s".format(clientId,clientSecret,code),"application/x-www-form-urlencoded")
    // FIXME: This could explode                                                                                                                           d
    Helpers.tryo {
      // make the POST
      val response = http(req.secure as_str)
      http.shutdown()
      // parse the token response
      val TokenResponse = "access_token=(.+)&token_type=(.+)".r
      val TokenResponse(token,typez) = response
      token
    }
  }
}

