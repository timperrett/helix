package helix.db

import helix.domain._

trait MongoRepositories extends Repositories {
  import net.liftweb.util.Props
  import com.novus.salat._
  import com.novus.salat.global._
  import com.novus.salat.dao._
  import com.mongodb.casbah.Imports._
  import com.mongodb.casbah.MongoURI
  
  class MongoRepository extends HelixRepository {
    /** lists for projects **/
    def listFiveNewestProjects: List[Project] = 
      ProjectDAO.find(MongoDBObject()).limit(5).toList
    
    /** global lists **/
    // def listAllTags: List[Tag]
    
    /** finders **/
    // def findContributorByLogin(login: String): Option[Contributor] //= 
      // ContributorDAO.findOne(MongoDBObject("login" -> login))
      
    def findProjectByGroupAndArtifact(group: String, artifact: String): Option[Project] = 
      ProjectDAO.findOne(MongoDBObject("groupId" -> group, "artifactId" -> artifact))
    
    /** creators **/
    def createProject(project: Project) = 
      !ProjectDAO.insert(project).isEmpty
    
    /** DAOs **/
    
    object ProjectDAO extends SalatDAO[Project, ObjectId](
      collection = mongo("projects")){
      // val contributors = new ChildCollection[Contributor, String]
      }
    
    private val mongo: MongoDB = {
      val db = MongoConnection(
        Props.get("mongo.host").openOr("localhost"), 
        Props.get("mongo.port").map(_.toInt).openOr(10011))(
        Props.get("mongo.db").openOr("helix")
      )
      if (db.authenticate(
        Props.get("mongo.username").openOr("user"), 
        Props.get("mongo.password").openOr("secret"))) db
      else throw new IllegalArgumentException("DEATH AND DESTRUCTION! PASSWORD FAILURE!")
    }
  }
}
