package helix.async 

import akka.actor.{Actor,ActorRef}
import akka.actor.{Actor,Scheduler}, Actor._
import akka.config.Supervision._
import akka.dispatch._
import akka.routing._
import helix.domain.Project
import java.util.concurrent.TimeUnit.{HOURS,MINUTES}

object Manager {
  def start(){
    // touch the agents
    Agents.all
    // start the actors
    List(
      actorOf[Statistics], 
      actorOf[ProjectManager]
    ).foreach(_.start())
  }
  def stop(){
    Actor.registry.shutdownAll
    Agents.all.foreach(_.close())
  }
}

object ProjectManager {
  case class UpdateAttributes(project: Project)
}
class ProjectManager extends Actor 
    with DefaultActorPool 
    with BoundedCapacityStrategy
    with ActiveFuturesPressureCapacitor 
    with SmallestMailboxSelector 
    with BasicNoBackoffFilter {
  
  self.faultHandler = OneForOneStrategy(classOf[RuntimeException] :: Nil, 10, 10000)
  
  def receive = _route
  def lowerBound = 2
  def upperBound = 4
  def rampupRate = 0.1
  def partialFill = true
  def selectionCount = 1
  def instance = actorOf(new ProjectWorker(self))
}
class ProjectWorker(owner: ActorRef) extends Actor {
  import ProjectManager._
  import helix.github.Client
  import helix.domain.Service
  
  def receive = {
    case msg@UpdateAttributes(project) => {
      (for {
        unr <- project.usernameAndRepository
        repo <- Client.repositoryInformation(unr)
        created <- repo.createdAt
        contributors = Client.contributorsFor(unr)
      } yield project.copy(
        contributors = contributors,
        contributorCount = contributors.size,
        watcherCount = repo.watchers.toLong,
        forkCount = repo.forks.toLong,
        createdAt = created.toDate,
        setupComplete = true,
        updatedAt = new java.util.Date
      )) map(_.copy(activityScore = Service.calculateProjectAggregateScore(project)
      )) foreach(p => Service.updateProject(p.id, p))
      // this is not persisted and is only for dev purposes!
      // Scheduler.scheduleOnce(owner, msg, 3, MINUTES)
    }
  }
}

/**
 * This actor handles pure background tasks
 * for computation and aggregation of global 
 * statistics that are used by multiple parts
 * of the application
 */
object Statistics {
  case object UpdateTotalProjectCount
  case object UpdateAverageProjectForkCount
  case object UpdateAverageProjectWatcherCount
}
class Statistics extends Actor {
  import helix.domain.Service._
  import Statistics._ 
  import Agents._
  
  self.faultHandler = OneForOneStrategy(classOf[Exception] :: Nil, 10, 100)
  
  def receive = { 
    case msg@UpdateTotalProjectCount => 
      TotalProjectCount send findAllProjectCount
      Scheduler.scheduleOnce(self, msg, 6, MINUTES)
      
    case msg@UpdateAverageProjectWatcherCount =>
      AverageProjectWatcherCount send findAverageWatcherCount
      Scheduler.scheduleOnce(self, msg, 3, MINUTES)
    
    case msg@UpdateAverageProjectForkCount =>
      AverageProjectForkCount send findAverageForkCount
      Scheduler.scheduleOnce(self, msg, 3, MINUTES)
      
  }
  
  override def preStart {
    List(
      UpdateTotalProjectCount, 
      UpdateAverageProjectWatcherCount,
      UpdateAverageProjectForkCount
    ).foreach(self ! _)
  }
}