package helix.domain

import java.sql.Timestamp
import net.liftweb.util.{Props,Helpers}
import helix.db.MongoRepositories
import helix.github.GithubScoring

object Service extends HelixService with MongoRepositories with GithubScoring with Statistics {
  protected val repository = new MongoRepository
  protected val scoring = new DefaultGithubScoring
}

import helix.lib.{Repositories,Scoring}

trait HelixService { _: Repositories with Scoring with Statistics => 
  def calculateProjectActivityScore(p: Project) = 
    scoring.calculateProjectActivityScore(p)
  
  // this may need revising, it feels wrong.
  def createProject(p: Project): Boolean = 
    (for(project <- repository.createProject(p)) yield {
      // Whatver ! project // notify the events actor that a new project has been added.
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
  
  def updateProject[T](id: T, project: Project): Unit = 
    repository.updateProject(id, project)
}

trait Statistics {
  def totalProjectCount: Long = 
    helix.async.Agents.TotalProjectCount.get
}
