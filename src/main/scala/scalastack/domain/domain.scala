package scalastack.domain

trait ReleaseType
case object Final extends ReleaseType
case object RC extends ReleaseType
case object Snapshot extends ReleaseType

case class Project(id: Int, name: String)

case class Contributor(name: String, login: String, style: String, avatar: String)
