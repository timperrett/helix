package helix.http

object Vars {
  import net.liftweb.http.SessionVar
  import helix.domain.Contributor
  
  object AccessToken extends SessionVar[Option[String]](None)
  object CurrentContributor extends SessionVar[Option[Contributor]](None)
}