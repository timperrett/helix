package helix.domain

import helix.db.MongoRepositories
import helix.github.{GithubScoring,GithubAPIClients}

object Service extends HelixService 
    with MongoRepositories 
    with GithubAPIClients
    with GithubScoring
    with AgentStatistics {
  protected val repository = new MongoRepository
  protected val scoring = new AlphaGithubScoring
  // this needs reviewing. not sure about it yet.
  val github = new GithubClient
}

import helix.lib.{Repositories,Scoring,Statistics}
import helix.async.ProjectManager
import akka.actor.Actor.registry.actorFor

trait HelixService { _: Repositories with Scoring with Statistics => 
  def calculateProjectAggregateScore(p: Project): Double = 
    scoring.calculateProjectAggregateScore(p)
  
  // this may need revising, it feels wrong.
  def createProject(p: Project): Boolean = 
    (for(project <- repository.createProject(p)) yield {
      for(a <- actorFor[ProjectManager]){
        a ! ProjectManager.UpdateAttributes(project)
      }
      true
    }) getOrElse false
  
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
  
  def findStaleProjects: List[Project] = 
    repository.findStaleProjects(
      new org.joda.time.DateTime().minusDays(2).getMillis)
  
  def updateProject[T](id: T, project: Project): Unit = 
    repository.updateProject(id, project)
}

// readers for global agent state
trait AgentStatistics extends Statistics {
  import helix.async.Agents._
  def totalProjectCount: Long = TotalProjectCount.get
  def averageProjectWatcherCount: Double = AverageProjectWatcherCount.get
  def averageProjectForkCount: Double = AverageProjectForkCount.get
}
