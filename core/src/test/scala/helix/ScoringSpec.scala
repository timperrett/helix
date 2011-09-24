package helix.test

import org.specs2.mutable._
import Sandbox._

import helix.lib.{Statistics,GithubClients}
import helix.github.{GithubAPIClients,GithubScoring}
import helix.domain.Project

class ScoringSpec extends Specification with DummyProjects {
  "When project has commits Github scoring algorith" should {
    // lift-shiro
    "Determine the comunity score to be '28.13347941021377'" in {
      DummyWith11Commits.calculateProjectComunityScore(liftShiro) must_== 28.13347941021377D
    }
    "Determine the activity score to be '8.642857142857142'" in {
      DummyWith11Commits.calculateProjectActivityScore(liftShiro) must_== 8.642857142857142D
    }
    "Determine the aggregate score of '36.77633655307091'" in {
      DummyWith11Commits.calculateProjectAggregateScore(liftShiro) must_== 36.77633655307091D
    }
    // scalaz
    "Determine the activity score to be '0.01020408163265306' when 1 commit is presnt" in {
      DummyWith1Commits.calculateProjectActivityScore(scalaz) must_== 0.01020408163265306D
    }
  }
  "When project has zero commits Github scoring algorith" should {
    "Determine the activity score to be zero" in {
      DummyWithNoCommits.calculateProjectActivityScore(liftShiro) must_== 0D
    }
  }
  // step(setup)
  // step(teardown)
}

trait DummyProjects {
  val liftShiro = Project(
    name = "Dummy Project",
    sourceURL = Some("https://github.com/timperrett/lift-shiro"),
    contributorCount = 2,
    watcherCount = 24,
    forkCount = 4)
    
  val scalaz = Project(
    name = "Dummy Project",
    sourceURL = Some("https://github.com/timperrett/lift-shiro"),
    contributorCount = 14,
    watcherCount = 286,
    forkCount = 39)
}


// see excel file for calculation expectations. 
trait DummyStatistics extends Statistics {
  def totalProjectCount: Long = 24L
  def averageProjectWatcherCount: Double = 268.375D
  def averageProjectForkCount: Double = 30.29166667D
}

trait DummyScoringService extends GithubScoring with DummyStatistics with GithubAPIClients {
  val github = new GithubClient
  // dummy accessors
  def calculateProjectAggregateScore(p: Project): Double = 
    scoring.calculateProjectAggregateScore(p)
  
  def calculateProjectComunityScore(project: Project): Double = 
    scoring.calculateProjectComunityScore(project)
  
  def calculateProjectActivityScore(project: Project): Double = 
    scoring.calculateProjectActivityScore(project)
}

import GithubClients._

object DummyWith11Commits extends DummyScoringService {
  protected val scoring = new AlphaGithubScoring {
    override protected def commitsForProject(project: Project): List[Commit] = 
      (1 to 11).map(x => Commit(x.toString, "")).toList
  }
}

object DummyWith1Commits extends DummyScoringService {
  protected val scoring = new AlphaGithubScoring {
    override protected def commitsForProject(project: Project): List[Commit] = 
      Commit("", "") :: Nil
  }
}

object DummyWithNoCommits extends DummyScoringService {
  protected val scoring = new AlphaGithubScoring {
    override protected def commitsForProject(project: Project): List[Commit] = Nil
  }
}

