package helix.domain

import helix.db.MongoRepositories
import helix.github.{GithubScoring,GithubAPIClients}
import helix.search.ESSearching

object Service extends HelixService 
    with MongoRepositories 
    with GithubAPIClients
    with GithubScoring
    with AgentStatistics
    with ESSearching {
  protected val repository = new MongoRepository
  protected val scoring = new AlphaGithubScoring
  val searching = new ESSearchProvider
  // this needs reviewing. not sure about it yet.
  val github = new GithubClient
}

import helix.util.Config._
import helix.lib.{Repositories,Scoring,Statistics,Searching}
import helix.async.ProjectManager
import akka.actor.Actor.registry.actorFor

trait HelixService { _: Repositories with Scoring with Statistics with Searching => 
  
  // kestral combinator ftw!
  protected def kestrel[T](x: T)(f: T => Unit): T = {
    f(x); x 
  }
  
  def asyncronuslyUpdate(project: Project) {
    for(a <- actorFor[ProjectManager]){
      a ! ProjectManager.UpdateAttributes(project)
    }
  }
  
  def calculateProjectAggregateScore(p: Project): Double = 
    scoring.calculateProjectAggregateScore(p)
  
  def createScalaVersion(v: ScalaVersion) = 
    repository.createScalaVersion(v)
  
  def listScalaVersions: List[ScalaVersion] = 
    repository.listScalaVersions
  
  def listProjectsAlphabetically(limit: Int = 10, offset: Int = 0): List[Project] = 
    repository.listProjectsAlphabetically(limit,offset)
  
  def listFiveNewestProjects: List[Project] = 
    repository.listFiveNewestProjects
  
  def listFiveMostActiveProjects: List[Project] = 
    repository.listFiveMostActiveProjects
  
  def findProjectByGroupAndArtifact(group: String, artifact: String) = 
    repository.findProjectByGroupAndArtifact(group,artifact)
  
  def findAllProjectCount: Long = 
    repository.findAllProjectCount
  
  def findAverageForkCount: Double = 
    repository.findAverageForkCount
  
  def findAverageContributorCount: Double = 
    repository.findAverageContributorCount
  
  def findAverageWatcherCount: Double = 
    repository.findAverageWatcherCount
  
  def findStaleProjects: List[Project] = {
    import org.joda.time.DateTime
    def expiryLong(d: DateTime) = {
      val duration = Conf.get[Int]("project.stale.duration").getOrElse(1)
      (Conf.get[String]("project.stale.measure").getOrElse("days") match {
        case "months" | "month" => d.minusMonths(duration)
        case "days" | "day" => d.minusDays(duration)
        case "hours" | "hour" => d.minusHours(duration)
        case "minutes" | "minute" => d.minusMinutes(duration)
      }).getMillis
    }
    repository.findStaleProjects(expiryLong(new DateTime))
  }
  
  def findContributorByLogin(login: String): Option[Contributor] = 
    repository.findContributorByLogin(login)
  
  def saveContributor(contributor: Contributor): Option[Contributor] =
    (for(loaded <- repository.findContributorByLogin(contributor.login))
      yield kestrel(contributor){ c =>
        repository.updateContributor(loaded.id, contributor.copy(id = loaded.id))
      }) orElse repository.createContributor(contributor)
  
  def save(project: Project)(implicit f: Project => Unit = p => ()): Option[Project] = 
    kestrel {
      (for {
        group <- project.groupId
        artifact <- project.artifactId
        loaded <- findProjectByGroupAndArtifact(group, artifact)
      } yield kestrel(project){ p => 
        repository.updateProject(loaded.id, project.copy(id = loaded.id))
      }) orElse repository.createProject(project)
    }{ proj => 
      for(p <- proj){ f(p) }
    }
  
  def search(term: String): List[Project] = 
    searching.search(term)
  
  def addToSearchIndex(project: Project) = 
    searching.index(project)
  
}

// readers for global agent state
trait AgentStatistics extends Statistics {
  import helix.async.Agents._
  def totalProjectCount: Long = TotalProjectCount.get
  def averageProjectWatcherCount: Double = AverageProjectWatcherCount.get
  def averageProjectForkCount: Double = AverageProjectForkCount.get
}
