package helix.async 

import akka.agent.Agent
import helix.domain.Project

object Agents {
  // used to do the cleanup when the 
  // application shuts down. 
  private[async] val all: List[Agent[_]] = List(
    TotalProjectCount,
    AverageProjectWatcherCount,
    AverageProjectForkCount,
    AverageProjectContributorCount)
  
  // totals
  val TotalProjectCount = new Agent[Long](0L)
  
  // averages for activity calculation
  val AverageProjectWatcherCount     = new Agent[Double](1.0D)
  val AverageProjectForkCount        = new Agent[Double](1.0D)
  val AverageProjectContributorCount = new Agent[Double](1.0D)
}
