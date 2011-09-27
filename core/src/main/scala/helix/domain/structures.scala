package helix.domain

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.annotations.{Key,Salat}
import com.mongodb.casbah.Imports._
import org.joda.time.DateTime

case class Project(
  @Key("_id") id: ObjectId = new ObjectId, 
  name: String, 
  headline: Option[String] = None,
  description: Option[String] = None,
  groupId: Option[String] = None,
  artifactId: Option[String] = None,
  versions: Map[String, String] = Map.empty,
  modules: Map[String, String] = Map.empty,
  usagePhase: Option[String] = None, // compile, test, jetty etc
  repositoryURL: Option[String] = None, // maven repo
  sourceURL: Option[String] = None, // github repo
  addedBy: Option[String] = None, // addedBy represents the adder's github login
  addedAt: java.util.Date = new DateTime().toDate,
  createdAt: java.util.Date = new DateTime().toDate, // when the github repo was made
  updatedAt: Long = new DateTime().getMillis, // updated every time the async task runs
  contributors: List[Contributor] = Nil,
  tags: List[Tag] = Nil,
  // asyncrnous
  contributorCount: Long = 1L,
  forkCount: Long = 1L,
  watcherCount: Long = 1L,
  activityScore: Double = 0D,
  setupComplete: Boolean = false
){
  import helix.util.Random.randomSelect
  import helix.util.Hex
  
  def randomContributor: Option[Contributor] = 
    if(contributors.isEmpty) None
    else randomSelect(1, contributors).headOption
  
  def versionsDecoded = 
    versions.map(x => new String(Hex.decode(x._1)) -> x._2)
  
  def activity: Activity = 
    List(Obsolete, Quiet, Moderate, Fair, Busy, Hectic
      ).find(_.judge(activityScore)).getOrElse(UnknownActivity)
  
  val usernameAndRepository = sourceURL.map(_.substring(19))
}

case class Version(number: String, description: String, scalaVersions: List[ScalaVersion])

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

object ScoringPoints {
  val Zero = 0D
  val Band1 = 5D
  val Band2 = 20D
  val Band3 = 40D
  val Band4 = 60D
  val Band5 = 80D
}

import ScoringPoints._

sealed class Activity(val judge: Double => Boolean = _ == Zero)
object UnknownActivity extends Activity {
  override def toString = "Unknown"
}
object Obsolete extends Activity(_ <= Band1){
  override def toString = "Inactive"
}
object Quiet extends Activity(s => s > Band1 && s <= Band2){
  override def toString = "Quiet"
}
object Moderate extends Activity(s => s > Band2 && s <= Band3){
  override def toString = "Moderate"
}
object Fair extends Activity(s => s > Band3 && s <= Band4){
  override def toString = "Fair"
}
object Busy extends Activity(s => s > Band4 && s <= Band5){
  override def toString = "Busy"
}
object Hectic extends Activity(s => s > Band5){
  override def toString = "Hectic"
}

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
