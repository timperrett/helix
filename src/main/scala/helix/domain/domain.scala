package helix.domain

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.annotations.Key
import com.mongodb.casbah.Imports._
import net.liftweb.util.Helpers

trait ReleaseType
case object Final extends ReleaseType
case object RC extends ReleaseType
case object Snapshot extends ReleaseType

case class ScalaVersion(major: Int, minor: Int, mico: String, mode: ReleaseType)

case class Project(
  @Key("_id") id: ObjectId = new ObjectId, 
  name: String, 
  description: Option[String] = None,
  // permalink: Option[String] = None,
  groupId: Option[String] = None,
  artifactId: Option[String] = None,
  version: String = "0.1-SNAPSHOT",
  usagePhase: Option[String] = None,
  sourceURL: Option[String] = None,
  // addedBy represents the adder's github login
  addedBy: Option[String] = None,
  addedAt: java.util.Date = Helpers.now,
  contributors: List[Contributor] = Nil,
  tags: List[Tag] = Nil
)//{
  //import helix.db.Storage
  // def tags: List[Tag] = 
  //   if(tgs.isEmpty) Storage.listTagsForProject(id)
  //   else tgs
//}

case class Contributor(
  @Key("_id") login: String, 
  name: String, 
  avatar: Option[String] = None,
  style: String = "User"
)

case class Tag(name: String)