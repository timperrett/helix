package helix.domain

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.annotations.{Key,Salat}
import com.mongodb.casbah.Imports._
import net.liftweb.util.Helpers

// @Salat
sealed class Activity(val judge: Int => Boolean = _ == 0)
object UnknownActivity extends Activity {
  override def toString = "Unknown"
}
object Obsolete extends Activity(_ <= 5)
object Quiet extends Activity(s => s > 5 && s <= 20)
object Moderate extends Activity(s => s > 20 && s <= 40)
object Fair extends Activity(s => s > 40 && s <= 60)
object Busy extends Activity(s => s > 60 && s <= 80)
object Hectic extends Activity(s => s > 80)

case class Project(
  @Key("_id") id: ObjectId = new ObjectId, 
  name: String, 
  headline: Option[String] = None,
  description: Option[String] = None,
  groupId: Option[String] = None,
  artifactId: Option[String] = None,
  versions: Map[String, String] = Map.empty,
  usagePhase: Option[String] = None,
  repositoryURL: Option[String] = None, // maven repo
  sourceURL: Option[String] = None, // github repo
  // addedBy represents the adder's github login
  addedBy: Option[String] = None,
  addedAt: java.util.Date = Helpers.now,
  contributors: List[Contributor] = Nil,
  tags: List[Tag] = Nil,
  activityScore: Int = 0,
  latestSHA: Option[String] = None
){
  import helix.util.Random.randomSelect
  def randomContributor: Option[Contributor] = 
    randomSelect(1, contributors).headOption
  
  def versionsDecoded = 
    versions.map(x => new String(Helpers.hexDecode(x._1)) -> x._2)
  
  def activity: Activity = 
    List(Obsolete, Quiet, Moderate, Fair, Busy, Hectic
      ).find(_.judge(activityScore)).getOrElse(UnknownActivity)
  
  val usernameAndRepository = sourceURL.map(_.substring(19))
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

@Salat
trait ReleaseType

case object Final extends ReleaseType
case class RC(step: Int) extends ReleaseType
case object Snapshot extends ReleaseType

case class ScalaVersion(
  @Key("_id") id: ObjectId = new ObjectId, 
  major: Int, minor: Int, micro: String, 
  mode: ReleaseType){
  def asVersion = "%d.%d.%s".format(major,minor,micro) + (mode match {
    case Final => ".Final"
    case RC(step) => "-RC%d".format(step)
    case Snapshot => "-SNAPSHOT"
  })
}
