package helix.github

import helix.domain.Project
import helix.lib.Scoring

trait GithubScoring extends Scoring {
 
  // this needs cleaning up
  class AlphaGithubScoring extends ScoringStrategy {
    import org.joda.time.DateTime
    
    val DaysOfConsideredHistory = 7
    val zero = 0D
    def calculateProjectActivityScore(project: Project): Double = {
      (for {
        repo <- project.usernameAndRepository.toList
        list = Client.commitHistoryFor(repo)
        commit <- list
        date <- commit.when if date.isAfter(new DateTime().minusDays(DaysOfConsideredHistory))
      } yield {
        val count = list.size.toDouble
        if(count > zero) ((count / 7) * (count / project.contributorCount))
        else zero
      }).headOption.getOrElse(zero)
    }
  }
  
  // this must die.
  // class DefaultGithubScoring extends ScoringStrategy {
  //     /*
  //     + watchers 
  //     + commiters
  //     + number of forks / 2
  //     + number of commits in time delta
  //     * number of unique commiters within that commit delta
  //     */
  //     def calculateProjectActivityScore(project: Project): BigInt = (for {
  //       unr <- project.usernameAndRepository
  //       repo <- Client.repositoryInformation(unr)
  //       commits = Client.commitHistoryFor(unr)
  //     } yield {
  //       // println(">>>>>>>>>")
  //       // println("repo watchers: " + repo.watchers)
  //       // println("repo forks: " + repo.forks)
  //       // println("project contributors: " + project.contributors.size)
  //       // println("commits size: " + commits.size)
  //       // println("unique commiters" + commits.groupBy(_.by).keys.size)
  //       // 
  //       ((repo.watchers / 2) + 
  //       repo.forks + 
  //       project.contributors.size + 
  //       commits.size) //* commits.groupBy(_.by).keys.size
  //     }) getOrElse BigInt.int2bigInt(0)
  //   }
}
