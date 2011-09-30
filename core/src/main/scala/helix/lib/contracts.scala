package helix.lib

import helix.domain._

trait Repositories {
  protected def repository: HelixRepository
  
  trait HelixRepository {
    /** global lists **/
    def listProjectsAlphabetically(limit: Int, offset: Int): List[Project]
    def listFiveNewestProjects: List[Project]
    def listFiveMostActiveProjects: List[Project]
    def listScalaVersions: List[ScalaVersion]
    // def listAllTags: List[Tag]
    
    /** finders **/
    def findProjectByGroupAndArtifact(group: String, artifact: String): Option[Project]
    def findAllProjectCount: Long
    def findAverageContributorCount: Double
    def findAverageWatcherCount: Double
    def findAverageForkCount: Double
    def findStaleProjects(boundry: Long): List[Project]
    def findContributorByLogin(login: String): Option[Contributor]
    
    /** creator **/
    def createProject(project: Project): Option[Project]
    def createScalaVersion(version: ScalaVersion): Boolean
    def createContributor(contributor: Contributor): Option[Contributor]
    
    /** updaters **/
    def updateProject[T](id: T, project: Project): Unit
    def updateContributor[T](id: T, contrib: Contributor): Unit
  }
}

trait Scoring { _: Statistics => 
  protected def scoring: ScoringStrategy
  
  trait ScoringStrategy {
    def calculateProjectComunityScore(project: Project): Double
    def calculateProjectActivityScore(project: Project): Double
    def calculateProjectAggregateScore(project: Project): Double
  }
}

trait Statistics { 
  def totalProjectCount: Long
  def averageProjectWatcherCount: Double
  def averageProjectForkCount: Double
}

object GithubClients {
  import org.joda.time.DateTime

  case class Repo(
    watchers: BigInt, 
    forks: BigInt, 
    createdAt: Option[DateTime] = None,
    pushedAt: Option[DateTime] = None
  )
  
  case class Commit(owner: String, sha: String, when: Option[DateTime] = None)
}

trait GithubClients {
  import GithubClients._
  import helix.domain.Contributor
  import net.liftweb.json.JsonAST._
  
  def github: Client
  
  trait Client {
    protected def get[T](path: String, params: Map[String,String])(f: JValue => T): T
    def contributor(token: String): Option[Contributor]
    def contributorsFor(repo: String): List[Contributor]
    def repositoryInformation(on: String): Option[Repo]
    def commitHistoryFor(repo: String, sinceSha: Option[String] = None): List[Commit]
    def requestAccessToken(clientId: String, clientSecret: String, code: String): Option[String]
  }
}
