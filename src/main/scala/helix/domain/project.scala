package helix.domain

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.annotations.Key
import com.mongodb.casbah.Imports._
import net.liftweb.util.Helpers

trait Activity
case object Obsolete extends Activity
case object Quiet extends Activity
case object Moderate extends Activity
case object Fair extends Activity
case object Busy extends Activity
case object Hectic extends Activity

case class Project(
  @Key("_id") id: ObjectId = new ObjectId, 
  name: String, 
  headline: Option[String] = None,
  description: Option[String] = None,
  groupId: Option[String] = None,
  artifactId: Option[String] = None,
  versions: Map[String, List[ScalaVersion]] = Map.empty,
  usagePhase: Option[String] = None,
  repositoryURL: Option[String] = None, // maven repo
  sourceURL: Option[String] = None, // github repo
  // addedBy represents the adder's github login
  addedBy: Option[String] = None,
  addedAt: java.util.Date = Helpers.now,
  contributors: List[Contributor] = Nil,
  tags: List[Tag] = Nil,
  activity: Option[Activity] = None
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