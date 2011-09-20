package helix.async 

import akka.actor.Actor
import akka.actor.{Actor,Scheduler}, Actor._

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
  case object UpdateProjectAttributes
}
class ProjectManager extends Actor {
  import ProjectManager._
  def receive = {
    case UpdateProjectAttributes => 
      
  }
  override def preStart { }
}
class ProjectWorker extends Actor {
  def receive = {
    case _ => false
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
  case object UpdateAverageProjectContributorCount
  case object UpdateAverageProjectWatcherCount
}
class Statistics extends Actor {
  // println(">>>>>>>>>>>>>>>>>")
  
  import java.util.concurrent.TimeUnit.HOURS
  import akka.config.Supervision._
  import helix.domain.Service._
  import Statistics._ 
  import Agents._
  
  self.faultHandler = OneForOneStrategy(classOf[Exception] :: Nil, 10, 100)
  
  def receive = { 
    case UpdateTotalProjectCount => 
      TotalProjectCount send findAllProjectCount
      Scheduler.scheduleOnce(self, UpdateTotalProjectCount, 12, HOURS)
    
    case UpdateAverageProjectContributorCount => 
    
    case UpdateAverageProjectWatcherCount =>
    
  }
  
  override def preStart {
    List(UpdateTotalProjectCount).foreach(self ! _)
  }
}