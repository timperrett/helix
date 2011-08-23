package helix.domain

trait ReleaseType
case object Final extends ReleaseType
case object RC extends ReleaseType
case object Snapshot extends ReleaseType

case class ScalaVersion(major: Int, minor: Int, mico: String, mode: ReleaseType)

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
  contributor: Option[Contributor] = None,
  tgs: List[Tag] = Nil
){
  import helix.db.Storage
  def tags: List[Tag] = 
    if(tgs.isEmpty) Storage.listTagsForProject(id)
    else tgs
}

case class Contributor(
  name: String, 
  login: String, 
  avatar: Option[String] = None,
  style: String = "User"
)

case class Tag(name: String)