package helix.github

import helix.lib.GithubClients

// this whole thing needs renaming somehow vs the contracts
trait GithubAPIClients extends GithubClients {
  class GithubClient extends Client {
    import dispatch._
    import dispatch.json.Js._
    import dispatch.liftjson.Js._
    import net.liftweb.json.JsonAST._
    import org.joda.time.DateTime
    import helix.lib.GithubClients._
    import helix.domain.Contributor
    
    private def parseGithubDate(string: String): Option[DateTime] = 
      try {
        import org.joda.time.format.ISODateTimeFormat
        Some(ISODateTimeFormat.dateTimeNoMillis.parseDateTime(string))
      } catch {
        case e => None
      }
    
    protected def defaultRequestParameters: Map[String,String] = Map.empty
    
    def get[T](path: String, params: Map[String,String] = defaultRequestParameters)(f: JValue => T) = {
      val http = new Http
      val req = url("https://api.github.com" + path) <<? params
      var resp = http(req ># f)
      http.shutdown()
      resp
    }
    
    def contributor: Option[Contributor] = 
      get("/user"){ json => (for { 
          JObject(child) <- json
          JField("name", JString(name)) <- child
          JField("login", JString(login)) <- child
          JField("type", JString(style)) <- child
          JField("avatar_url", JString(url)) <- child
        } yield Contributor(
            login = login, name = Some(name), 
            avatar = Some(url), style = style)).headOption
      }
    
    def commitHistoryFor(repo: String, sinceSha: Option[String] = None): List[Commit] = 
      get("/repos/%s/commits".format(repo), sinceSha.map(sha => Map("sha" -> sha)
        ).getOrElse(Map.empty) ++ Map("per_page" -> "100")){ json =>
          for {
            JArray(commits) <- json
            JObject(commit) <- commits
            JField("sha", JString(sha)) <- commit
            JField("committer", JObject(committer)) <- commit
            JField("login", JString(owner)) <- committer
            JField("commit", JObject(innercommit)) <- commit
            JField("committer", JObject(innercommiter)) <- innercommit
            JField("date", JString(when)) <- innercommiter
          } yield Commit(owner, sha, parseGithubDate(when))
        }
    
    def repositoryInformation(on: String): Option[Repo] = 
      get("/repos/%s".format(on)){ json => 
        (for {
          JObject(info) <- json
          JField("watchers", JInt(watchers)) <- info
          JField("forks", JInt(forks)) <- info
          JField("pushed_at", JString(pushedAt)) <- info
          JField("created_at", JString(createdAt)) <- info
        } yield Repo(
          watchers, 
          forks, 
          parseGithubDate(createdAt),
          parseGithubDate(pushedAt)
        )).headOption
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
    
    // refactor this at a later date. its pretty fugly.
    def requestAccessToken(clientId: String, clientSecret: String, code: String): Option[String] = {
      import scala.util.control.Exception._
      val http = new Http
      // FIXME: This could explode                                                                                                                           d
      val req = url("https://github.com/login/oauth/access_token") << 
        ("client_id=%s&client_secret=%s&code=%s".format(
          clientId,clientSecret,code),"application/x-www-form-urlencoded")
      allCatch.opt {
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
}



// object Client {
//   // import net.liftweb.http.SessionVar
//   // import net.liftweb.common.{Box,Full,Empty}
//   // import net.liftweb.util.Helpers
//   
//   object AccessToken extends SessionVar[Box[String]](Empty)
//   object CurrentContributor extends SessionVar[Box[Contributor]](Empty)
//   
//   def isAuthenticated: Boolean = 
//     !AccessToken.is.isEmpty
//   
//   private def tokenMap = AccessToken.is.map(t => Map("access_token" -> t)).openOr(Map.empty)
//   
//   // FIXME: This is FUGLY. 
//   def requestAccessToken(clientId: String, clientSecret: String, code: String): Box[String] = {
//     val endpoint = "https://github.com/login/oauth/access_token"
//     val http = new Http
//     val req = url(endpoint) << ("client_id=%s&client_secret=%s&code=%s".format(clientId,clientSecret,code),"application/x-www-form-urlencoded")
//     // FIXME: This could explode                                                                                                                           d
//     Helpers.tryo {
//       // make the POST
//       val response = http(req.secure as_str)
//       http.shutdown()
//       // parse the token response
//       val TokenResponse = "access_token=(.+)&token_type=(.+)".r
//       val TokenResponse(token,typez) = response
//       token
//     }
//   }
// }

