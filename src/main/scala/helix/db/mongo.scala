package helix.db

trait MongoRepositories extends Repositories {
  import helix.domain._
  import net.liftweb.util.Props
  import net.liftweb.mongodb.{MongoDB, DefaultMongoIdentifier, MongoAddress, MongoHost}
  
  def connect(){
    MongoDB.defineDbAuth(DefaultMongoIdentifier, MongoAddress(
      MongoHost(
        Props.get("mongo.host").openOr("localhost"), 
        Props.get("mongo.port").openOr("27017").toInt
      ), 
      Props.get("mongo.db").openOr("helix")),
      Props.get("mongo.username").openOr("admin"),
      Props.get("mongo.password").openOr("secret"))
  }
  
  class MongoRepository extends HelixRepository {
    /** lists for projects **/
    def listTopFiveProjects: List[Project]
    def listTagsForProject(projectId: Int): List[Tag]
    def listScalaVersionsForProject(projectId: Int): List[ScalaVersion]
    
    /** global lists **/
    def listScalaVersions: List[ScalaVersion]
    def listAllTags: List[Tag]
    
    /** finders **/
    def findContributorByLogin(login: String): Option[Int]
    def findProjectByPermalink(link: String): Option[Project]
  }
}