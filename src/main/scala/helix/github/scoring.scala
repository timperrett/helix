package helix.github

import helix.domain.Project
import helix.lib.Scoring

trait GithubScoring extends Scoring {  
  class DefaultGithubScoring extends ScoringStrategy {
    
    /*
    https://api.github.com/repos/djspiewak/42omega
    created_at
    
    https://api.github.com/repos/djspiewak/42omega/commits/5ddbd17e3028ba0c2d9cd015343fbcac505e49b7
    size of array
    tail sha
    
    + watchers 
    + commiters
    + number of commits in time delta
    * number of unique commiters within that commit delta
    + number of foks / 2
    */
    def calculateProjectActivityScore(project: Project): BigInt = (for {
      unr <- project.usernameAndRepository
      repo <- Client.repositoryInformation(unr)
      // commits = Client.
    } yield {
      (repo.watchers / 2) + (repo.forks) + project.contributors.size
    }) getOrElse BigInt.int2bigInt(0)
  }
}
