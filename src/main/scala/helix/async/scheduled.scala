package helix.async 

import akka.agent.Agent

object TotalProjectCount extends Agent(0L)

import akka.actor.Actor
import akka.actor.{Actor,Scheduler}, Actor._

object DaliyRunner {
  case object UpdateProjectCount
}
class DaliyRunner extends Actor {
  import DaliyRunner._ 
  import helix.db.Storage._
  import akka.config.Supervision._
  import java.util.concurrent.TimeUnit.HOURS
  
  self.faultHandler = OneForOneStrategy(classOf[Exception] :: Nil, 10, 100)
  
  def receive = { 
    case UpdateProjectCount => 
      TotalProjectCount send findAllProjectCount
      Scheduler.scheduleOnce(self, UpdateProjectCount, 24, HOURS)
  }
  
  override def preStart {
    self ! UpdateProjectCount
  }
}