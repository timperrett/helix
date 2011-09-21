package helix.db

import helix.domain._
import helix.lib.Repositories

trait MongoRepositories extends Repositories {
  import net.liftweb.common.Full
  import net.liftweb.util.Props
  import com.novus.salat._
  import com.novus.salat.global._
  import com.novus.salat.dao._
  import com.mongodb.casbah.Imports._
  import com.mongodb.casbah.MongoURI
  
  class MongoRepository extends HelixRepository {
    def listProjectsAlphabetically(limit: Int, offset: Int): List[Project] = 
      ProjectDAO.find(MongoDBObject("setupComplete" -> true)).limit(limit)
        .sort(orderBy = MongoDBObject("name" -> 1)).toList
    
    /** lists for projects **/
    def listFiveNewestProjects: List[Project] = 
      ProjectDAO.find(MongoDBObject("setupComplete" -> true)
        ).limit(5).sort(orderBy = MongoDBObject("_id" -> -1)).toList
    
    def listFiveMostActiveProjects: List[Project] = 
      ProjectDAO.find(MongoDBObject("setupComplete" -> true)
        ).limit(5).sort(orderBy = MongoDBObject("activityScore" -> -1)).toList
    
    /** global lists **/
    def listScalaVersions = ScalaVersionDAO.find(MongoDBObject()
      ).sort(orderBy = MongoDBObject(
        "major" -> -1, "minor" -> -1, "micro" -> -1, "mode" -> 1)).toList
    
    // def listAllTags: List[Tag]
    
    /** finders **/
    def findProjectByGroupAndArtifact(group: String, artifact: String): Option[Project] = 
      ProjectDAO.findOne(MongoDBObject("groupId" -> group, "artifactId" -> artifact))
    
    def findAllProjectCount = ProjectDAO.count()
    
    // def findAverageContributorCount = 
    //   ProjectDAO.find(MongoDBObject()).group(
    //     MongoDBObject() // condition
    //     MongoDBObject("contributors.login" -> 1) // key
    //     MongoDBObject("count" -> 0) //initial
    //     "function(doc, out){ out.count++; out.total_time+=doc.response_time }"
    //   )
      
    /** creators **/
    def createProject(project: Project): Option[Project] = 
      for(r <- ProjectDAO.insert(project)) yield project
    
    def createScalaVersion(version: ScalaVersion) = 
      !ScalaVersionDAO.insert(version).isEmpty
    
    /** updaters **/
    // this is less than ideal, but there appears to be some 
    // nightmarish overloading issue and implicits within salat
    def updateProject[T](id: T, project: Project): Unit = 
      ProjectDAO.update(
        MongoDBObject("_id" -> id), project, false, false,
        new WriteConcern)
    
    // def createProjectVersion(project: Project) = 
      // ProjectDAO.update()
    
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
