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
    
    /** creator **/
    def createProject(project: Project): Option[Project]
    def createScalaVersion(version: ScalaVersion): Boolean
    
    /** updaters **/
    def updateProject[T](id: T, project: Project): Unit
    
  }
}

// trait Github {
//   protected def github: 
//   
//   
// }

trait Scoring { _: Statistics => 
  protected def scoring: ScoringStrategy
  
  trait ScoringStrategy {
    protected def calculateProjectComunityScore(project: Project): Double
    protected def calculateProjectActivityScore(project: Project): Double
    def calculateProjectAggregateScore(project: Project): Double = 
      calculateProjectComunityScore(project) + 
      calculateProjectActivityScore(project)
  }
}

trait Statistics { 
  def totalProjectCount: Long
  def averageProjectWatcherCount: Double
  def averageProjectForkCount: Double
}

