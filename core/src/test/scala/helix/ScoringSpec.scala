package helix.test

import org.specs2.mutable._
import Sandbox._

import helix.lib.{Statistics,GithubClients}
import helix.github.{GithubAPIClients,GithubScoring}
import helix.domain.Project

class ScoringSpec extends Specification {
  import DummyScoring._
  
  val project = Project(
    name = "Dummy Project",
    sourceURL = Some("https://github.com/timperrett/lift-shiro"),
    contributorCount = 2,
    watcherCount = 24,
    forkCount = 4)
  
  "Github scoring algorithm" should {
    "Determine the comunity score to be '28.13347941021377'" in {
      calculateProjectComunityScore(project) must_== 28.13347941021377D
    }
    "Determine the activity score to be '8.642857142857142'" in {
      calculateProjectActivityScore(project) must_== 8.642857142857142D
    }
    "Determine the aggregate score of '36.77633655307091'" in {
      calculateProjectAggregateScore(project) must_== 36.77633655307091D
    }
  }
    
  // step(setup)
  // step(teardown)
}


// see excel file for calculation expectations. 
trait DummyStatistics extends Statistics {
  def totalProjectCount: Long = 24L
  def averageProjectWatcherCount: Double = 268.375D
  def averageProjectForkCount: Double = 30.29166667D
}

object DummyScoring extends GithubScoring with DummyStatistics with GithubAPIClients {
  import GithubClients._
  
  protected val scoring = new AlphaGithubScoring {
    override protected def commitsForProject(project: Project): List[Commit] = 
      (1 to 11).map(x => Commit(x.toString, "")).toList
  }
  val github = new GithubClient
  // dummy accessors
  def calculateProjectAggregateScore(p: Project): Double = 
    scoring.calculateProjectAggregateScore(p)
  
  def calculateProjectComunityScore(project: Project): Double = 
    scoring.calculateProjectComunityScore(project)
  
  def calculateProjectActivityScore(project: Project): Double = 
    scoring.calculateProjectActivityScore(project)
  
}