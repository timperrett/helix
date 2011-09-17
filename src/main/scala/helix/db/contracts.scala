package helix.db

import helix.domain._

trait Repositories {
  protected val repository: HelixRepository
  
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
    
    /** creator **/
    def createProject(project: Project): Boolean
    def createScalaVersion(version: ScalaVersion): Boolean
  }
}

