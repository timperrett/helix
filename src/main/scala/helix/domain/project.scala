package helix.domain

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.annotations.Key
import com.mongodb.casbah.Imports._
import net.liftweb.util.Helpers

case class Project(
  @Key("_id") id: ObjectId = new ObjectId, 
  name: String, 
  headline: Option[String] = None,
  description: Option[String] = None,
  // permalink: Option[String] = None,
  groupId: Option[String] = None,
  artifactId: Option[String] = None,
  //version: String = "0.1-SNAPSHOT",
  versions: Map[String, List[ScalaVersion]] = Map.empty,
  usagePhase: Option[String] = None,
  repositoryURL: Option[String] = None,
  sourceURL: Option[String] = None,
  // addedBy represents the adder's github login
  addedBy: Option[String] = None,
  addedAt: java.util.Date = Helpers.now,
  contributors: List[Contributor] = Nil,
  tags: List[Tag] = Nil
){
  import helix.util.Random.randomSelect
  def randomContributor: Option[Contributor] = 
    randomSelect(1, contributors).headOption
  def versionsDecoded = 
    versions.map(x => new String(Helpers.hexDecode(x._1)))
}

case class Contributor(
  login: String, 
  name: Option[String] = None, 
  avatar: Option[String] = None,
  contributions: Int = 0,
  style: String = "User"
){
  def picture = avatar getOrElse "http://gravatar.com/unknown"
}

case class Tag(name: String)