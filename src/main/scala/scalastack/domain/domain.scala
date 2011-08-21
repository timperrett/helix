package scalastack.domain

trait ReleaseType
case object Final extends ReleaseType
case object RC extends ReleaseType
case object Snapshot extends ReleaseType

case class Project(id: Int, name: String, 
  description: Option[String] = None,
  permalink: Option[String] = None,
  groupId: Option[String] = None,
  artifactId: Option[String] = None,
  usagePhase: Option[String] = None,
  sourceURL: Option[String] = None,
  addedAt: Option[java.sql.Timestamp] = None,
  addedBy: Option[String] = None,
  currentVersion: Option[String] = None,
  contributor: Option[Contributor] = None
)

case class Contributor(
  name: String, 
  login: String, 
  avatar: Option[String] = None,
  style: String = "User"
)
