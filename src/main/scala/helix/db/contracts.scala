package helix.db

import helix.domain._

trait Repositories {
  protected val repository: HelixRepository
  
  trait HelixRepository {
    /** lists for projects **/
    def listTagsForProject(projectId: Int): List[Tag]
    def listScalaVersionsForProject(projectId: Int): List[ScalaVersion]
    
    /** global lists **/
    def listFiveNewestProjects: List[Project]
    def listScalaVersions: List[ScalaVersion]
    def listAllTags: List[Tag]
    
    /** finders **/
    def findContributorByLogin(login: String): Option[Int]
    def findProjectByPermalink(link: String): Option[Project]
    
    /** creator **/
    def createContributor(contributor: Contributor): Unit
  }
}

trait HelixService { _: Repositories => 
  def listFiveNewestProjects: List[Project] = 
    repository.listFiveNewestProjects
  
  def listTagsForProject(id: Int): List[Tag] = 
    repository.listTagsForProject(id)
  
  def findContributorByLogin(login: String): Option[Int] = 
    repository.findContributorByLogin(login)
    
  def findProjectByPermalink(link: String): Option[Project] = 
    repository.findProjectByPermalink(link)
    
  def createContributor(c: Contributor): Unit = 
    repository.createContributor(c)
}
