package helix.db

import helix.domain._
import helix.lib.Repositories

trait MongoRepositories extends Repositories {
  // import net.liftweb.common.Full
  // import net.liftweb.util.Props
  import helix.util.Config._
  import com.novus.salat._
  import com.novus.salat.global._
  import com.novus.salat.dao._
  import com.mongodb.casbah.Imports._
  import com.mongodb.casbah.MongoURI
  
  class MongoRepository extends HelixRepository {
    def listProjectsAlphabetically(limit: Int, offset: Int): List[Project] = 
      ProjectDAO.find(MongoDBObject("setupComplete" -> true))
        .limit(limit)
        .skip(offset)
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
    
    def findAverageForkCount: Double =
      ProjectDAO.group(
        MongoDBObject(), // condition
        MongoDBObject(), // key
        MongoDBObject("count" -> 0, "forks" -> 0), //initial
        "function(doc, out){ out.count++; out.forks+=doc.forkCount;}",
        "function(out){ out.average = out.forks / out.count; }").lastOption.map(
          _.get("average").asInstanceOf[Double]).getOrElse(0D)
      
    def findAverageWatcherCount: Double = 
      ProjectDAO.group(
        MongoDBObject(), // condition
        MongoDBObject(), // key
        MongoDBObject("count" -> 0, "watchers" -> 0), //initial
        "function(doc, out){ out.count++; out.watchers+=doc.watcherCount;}",
        "function(out){ out.average = out.watchers / out.count; }").lastOption.map(
          _.get("average").asInstanceOf[Double]).getOrElse(0D)

    def findAverageContributorCount: Double = 
      ProjectDAO.group(
        MongoDBObject(), // condition
        MongoDBObject(), // key
        MongoDBObject("count" -> 0, "contributors" -> 0), //initial
        "function(doc, out){ out.count++; out.contributors+=doc.contributorCount;}",
        "function(out){ out.average = out.contributors / out.count; }").lastOption.map(
          _.get("average").asInstanceOf[Double]).getOrElse(0D)
      
    def findStaleProjects(boundry: Long): List[Project] =
      ProjectDAO.find("updatedAt" $lt boundry).toList
    
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
        Conf.get[String]("mongo.host").getOrElse("localhost"), 
        Conf.get[Int]("mongo.port").getOrElse(27017))(
        Conf.get[String]("mongo.db").getOrElse("helix")
      )
      
      // if the env specifies username and password, try to use
      // them, otherwise, just try to connect without auth.
      (Conf.get[String]("mongo.username"), Conf.get[String]("mongo.password")) match {
        case (Some(username), Some(password)) => 
          if(db.authenticate(username, password)) db
          else throw new IllegalArgumentException("Inavlid username and/or password")
        case _ => db
      }
    }
    
    /** DAOs **/
    object ProjectDAO extends SalatDAO[Project, ObjectId](collection = mongo("projects")){
      import scala.collection.mutable.ArrayBuffer
      import scala.collection.JavaConverters._
      // this is a hack to work around a bug within Casbah:
      // https://twitter.com/#!/rit/status/116531065513967617
      def group[A <% DBObject, B <% DBObject, C <% DBObject](key: A, cond: B, initial: C, reduce: String, finalize: String): List[DBObject] = {
        val cmd = MongoDBObject(
          "ns" -> collection.getName,
          "key" -> key,
          "cond" -> cond,
          "$reduce" -> reduce,
          "initial" -> initial,
          "finalize" -> finalize)
        val result = collection.getDB.command(MongoDBObject("group" -> cmd))
        result.get("retval").asInstanceOf[DBObject].toMap.asScala
          .map(_._2.asInstanceOf[DBObject]).asInstanceOf[ArrayBuffer[DBObject]].toList
      }
    }
    
    object ScalaVersionDAO extends SalatDAO[ScalaVersion, ObjectId](
      collection = mongo("scala_versions"))
  }
}
