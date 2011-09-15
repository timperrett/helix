package helix.actor 

import akka.actor.Actor
import akka.actor.{Actor,Scheduler}, Actor._

object SystemStatistics {
  case object UpdateProjectCount
  case object GetProjectCount
}
class SystemStatistics extends Actor {
  import SystemStatistics._ 
  import helix.db.Storage._
  import akka.config.Supervision._
  import java.util.concurrent.TimeUnit.HOURS
  
  private var projectCount: Long = 0L
  
  self.faultHandler = OneForOneStrategy(classOf[Exception] :: Nil, 10, 100)
  
  def receive = { 
    case UpdateProjectCount =>
      projectCount = findAllProjectCount
      Scheduler.scheduleOnce(self, 
        UpdateProjectCount, 24, HOURS)
      
    case GetProjectCount => {
      println(">>>>>>>>>>>>>>>>>>>>>>>>>>> %s".format(projectCount))
      projectCount
    }
  }
  
  override def preStart {
    self ! UpdateProjectCount
  }
}