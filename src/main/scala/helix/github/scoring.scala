package helix.github

import helix.domain.Project
import helix.lib.Scoring

trait GithubScoring extends Scoring {  
  class DefaultGithubScoring extends ScoringStrategy {
    private case class Repo(watchers: Int, forks: Int)
    private case class Commits(count: Int)
    
    /*
    
    + watchers 
    + commiters
    + number of commits in time delta
    * number of unique commiters within that commit delta
    + number of foks / 2
    */
    def calculateProjectActivityScore(project: Project): Int = 0
      // Client.get("/repos/%s".format(project.usernameAndRepository)){ json => 
      //   RepoData()
      // }
    
  }
}
