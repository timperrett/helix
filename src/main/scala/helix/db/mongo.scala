package helix.db

import helix.domain._

trait MongoRepositories extends Repositories {
  import net.liftweb.common.Full
  import net.liftweb.util.Props
  import com.novus.salat._
  import com.novus.salat.global._
  import com.novus.salat.dao._
  import com.mongodb.casbah.Imports._
  import com.mongodb.casbah.MongoURI
  
  class MongoRepository extends HelixRepository {
    /** lists for projects **/
    def listFiveNewestProjects: List[Project] = 
      ProjectDAO.find(MongoDBObject()
        ).limit(5).sort(orderBy = MongoDBObject("_id" -> -1)).toList
    
    /** global lists **/
    def listScalaVersions = ScalaVersionDAO.find(MongoDBObject()
      ).sort(orderBy = MongoDBObject("major" -> -1)).toList
    
    // def listAllTags: List[Tag]
    
    /** finders **/
    // def findContributorByLogin(login: String): Option[Contributor] //= 
      // ContributorDAO.findOne(MongoDBObject("login" -> login))
      
    def findProjectByGroupAndArtifact(group: String, artifact: String): Option[Project] = 
      ProjectDAO.findOne(MongoDBObject("groupId" -> group, "artifactId" -> artifact))
    
    /** creators **/
    def createProject(project: Project): Boolean = 
      !ProjectDAO.insert(project).isEmpty
    
    def createScalaVersion(version: ScalaVersion) = 
      !ScalaVersionDAO.insert(version).isEmpty
    
    /** internals **/
    private lazy val mongo: MongoDB = {
      val db = MongoConnection(
        Props.get("mongo.host").openOr("localhost"), 
        Props.get("mongo.port").map(_.toInt).openOr(27017))(
        Props.get("mongo.db").openOr("helix")
      )
      
      // if the env specifies username and password, try to use
      // them, otherwise, just try to connect without auth.
      (Props.get("mongo.username"), Props.get("mongo.password")) match {
        case (Full(username), Full(password)) => 
          if(db.authenticate(username, password)) db
          else throw new IllegalArgumentException("Inavlid username and/or password")
        case _ => db
      }
    }
    
    /** DAOs **/
    object ProjectDAO extends SalatDAO[Project, ObjectId](
      collection = mongo("projects"))
    
    object ScalaVersionDAO extends SalatDAO[ScalaVersion, ObjectId](
      collection = mongo("scala_versions"))
  }
}
