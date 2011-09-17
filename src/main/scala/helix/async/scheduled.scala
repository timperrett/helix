package helix.async 

import akka.agent.Agent
import helix.domain.Project

object TotalProjectCount extends Agent(0L)
object MostPopularProjects extends Agent[List[Project]](Nil)

import akka.actor.Actor
import akka.actor.{Actor,Scheduler}, Actor._

object DaliyRunner {
  case object UpdateProjectCount
  case object UpdateMostPopular
  case object UpdateProjectActivity
}
class DaliyRunner extends Actor {
  import DaliyRunner._ 
  import helix.domain.Service._
  import akka.config.Supervision._
  import java.util.concurrent.TimeUnit.HOURS
  
  self.faultHandler = OneForOneStrategy(classOf[Exception] :: Nil, 10, 100)
  
  def receive = { 
    case UpdateProjectCount => 
      TotalProjectCount send findAllProjectCount
      Scheduler.scheduleOnce(self, UpdateProjectCount, 24, HOURS)
    
    case UpdateMostPopular => 
  }
  
  override def preStart {
    List(UpdateProjectCount,UpdateMostPopular).foreach(self ! _)
  }
}