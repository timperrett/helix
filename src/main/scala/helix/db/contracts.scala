package helix.db

import helix.domain._

trait Repositories {
  protected val repository: HelixRepository
  
  trait HelixRepository {
    /** lists for projects **/
    // def listTagsForProject(projectId: Int): List[Tag]
    // def listScalaVersionsForProject(projectId: Int): List[ScalaVersion]
    
    /** global lists **/
    def listProjectsAlphabetically(limit: Int, offset: Int): List[Project]
    def listFiveNewestProjects: List[Project]
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

trait HelixService { _: Repositories => 
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
  
  def findProjectByGroupAndArtifact(group: String, artifact: String) = 
    repository.findProjectByGroupAndArtifact(group,artifact)
  
  def findAllProjectCount: Long = 
    repository.findAllProjectCount
  
  // def listTagsForProject(id: Int): List[Tag] = 
  //   repository.listTagsForProject(id)
  
  // def findContributorByLogin(login: String): Option[Contributor] = 
  //   repository.findContributorByLogin(login)
    
  // def findProjectByPermalink(link: String): Option[Project] = 
  //   repository.findProjectByPermalink(link)
    
  // def createContributor(contributor: Contributor): Contributor = {
  //   repository.createContributor(contributor)
  //   // TODO: Put error handling in place here
  //   contributor
  // }
}
