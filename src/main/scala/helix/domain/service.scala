package helix.domain

import java.sql.Timestamp
import net.liftweb.util.{Props,Helpers}
import helix.db.MongoRepositories
import helix.github.GithubScoring

object Service extends HelixService with MongoRepositories with GithubScoring {
  protected val repository = new MongoRepository
  protected val scoring = new DefaultGithubScoring
}

import helix.lib.{Repositories,Scoring}

trait HelixService { _: Repositories with Scoring => 
  def calculateProjectActivityScore(p: Project) = 
    scoring.calculateProjectActivityScore(p)
  
  def createProject(p: Project) = 
    repository.createProject(p)
  
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
  
  def updateProject(id: String, project: Project): Unit = 
    repository.updateProject(id, project)
}
