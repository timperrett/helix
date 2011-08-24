package helix.db

import helix.domain._

trait Repositories {
  protected val repository: HelixRepository
  
  trait HelixRepository {
    /** lists for projects **/
    // def listTagsForProject(projectId: Int): List[Tag]
    // def listScalaVersionsForProject(projectId: Int): List[ScalaVersion]
    
    /** global lists **/
    def listFiveNewestProjects: List[Project]
    // def listScalaVersions: List[ScalaVersion]
    // def listAllTags: List[Tag]
    
    /** finders **/
    def findProjectByGroupAndArtifact(group: String, artifact: String): List[Project]
    
    /** creator **/
    def createProject(project: Project): Boolean
  }
}

trait HelixService { _: Repositories => 
  def createProject(p: Project) = 
    repository.createProject(p)
  
  def listFiveNewestProjects: List[Project] = 
    repository.listFiveNewestProjects
  
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
