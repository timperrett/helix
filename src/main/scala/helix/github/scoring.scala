package helix.github

import helix.domain.Project
import helix.lib.Scoring

trait GithubScoring extends Scoring {
  
  /**
    br = ( (avg_num_commits * avg_rating) + (this_num_votes * this_rating) 
         ) / (avg_num_commits + this_num_commits)
  */
  
  // class BayesianGithubScoring extends ScoringStrategy {
  //   def calculateProjectActivityScore(project: Project): BigInt = (for {
  //     unr <- project.usernameAndRepository
  //     repo <- Client.repositoryInformation(unr)
  //     commits = Client.commitHistoryFor(unr)
  //   } yield {
  //     
  //   }) getOrElse BigInt.int2bigInt(0)
  // }
  
  
  class DefaultGithubScoring extends ScoringStrategy {
    /*
    + watchers 
    + commiters
    + number of forks / 2
    + number of commits in time delta
    * number of unique commiters within that commit delta
    */
    def calculateProjectActivityScore(project: Project): BigInt = (for {
      unr <- project.usernameAndRepository
      repo <- Client.repositoryInformation(unr)
      commits = Client.commitHistoryFor(unr)
    } yield {
      // println(">>>>>>>>>")
      // println("repo watchers: " + repo.watchers)
      // println("repo forks: " + repo.forks)
      // println("project contributors: " + project.contributors.size)
      // println("commits size: " + commits.size)
      // println("unique commiters" + commits.groupBy(_.by).keys.size)
      // 
      ((repo.watchers / 2) + 
      repo.forks + 
      project.contributors.size + 
      commits.size) * commits.groupBy(_.by).keys.size
    }) getOrElse BigInt.int2bigInt(0)
  }
}
