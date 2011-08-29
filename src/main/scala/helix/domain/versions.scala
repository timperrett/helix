package helix.domain

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.annotations.Salat
import com.mongodb.casbah.Imports._
import net.liftweb.util.Helpers

@Salat
trait ReleaseType

case object Final extends ReleaseType

case class RC(step: Int) extends ReleaseType

case object Snapshot extends ReleaseType

case class ScalaVersion(major: Int, minor: Int, micro: String, mode: ReleaseType){
  override def toString = "%d.%d.%s".format(major,minor,micro) + (mode match {
    case Final => ".Final"
    case RC(step) => "-RC%d".format(step)
    case Snapshot => "-SNAPSHOT"
  })
}

// object ScalaVersions {
//   def all = 
//     ScalaVersion(2,8,"1",Final) :: 
//     ScalaVersion(2,9,"0-1",Final) :: 
//     ScalaVersion(2,9,"1",RC(1))
// }