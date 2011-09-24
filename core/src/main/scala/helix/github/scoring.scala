package helix.github

import helix.domain.Project
import helix.lib.{Scoring,Statistics,GithubClients}

trait GithubScoring extends Scoring { _: Statistics with GithubClients => 
 
  // this needs cleaning up. really shouldnt be 
  // tied directly to the github client. needs some
  // level of abstraction
  class AlphaGithubScoring extends ScoringStrategy {
    import org.joda.time.DateTime
    import GithubClients.Commit
    
    private val DaysOfConsideredHistory = 7
    private val Zero = 0D
    
    protected def commitsForProject(project: Project): List[Commit] = for {
      commit <- github.commitHistoryFor(project.usernameAndRepository.get) 
      date <- commit.when if date.isAfter(new DateTime().minusDays(DaysOfConsideredHistory))
    } yield commit
    
    def calculateProjectAggregateScore(project: Project): Double =
      calculateProjectComunityScore(project) + 
      calculateProjectActivityScore(project)
    
    def calculateProjectComunityScore(project: Project): Double = 
      ((averageProjectWatcherCount * averageProjectForkCount) + 
      (project.watcherCount * project.forkCount)) / (averageProjectWatcherCount + project.watcherCount)
    
    def calculateProjectActivityScore(project: Project): Double = {
      val count = commitsForProject(project).size.toDouble
      if(count > Zero) ((count / DaysOfConsideredHistory) * (count / project.contributorCount))
      else Zero
    }
  }
  
}
