package helix.db

import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.ql.extended.{ExtendedTable => Table}
import net.liftweb.util.{Props,Helpers}
import java.sql.Timestamp
import helix.domain._

object Storage extends Connection with Queries with Tables {
  def listNewestAdded: List[Project] = db withSession {
    for(p <- ListTopFiveProjects.list) 
      yield Project(p._1,p._2,Some(p._3), 
        permalink = Some(p._4),
        addedAt = Some(p._5),
        contributor = Some(Contributor(p._6,p._7,Some(p._8)))
      )
  }
  
  def listTagsForProject(id: Int) = db withSession {
    for(t <- ListTagsForProject(id).list) yield Tag(t._2)
  }
  
  def findContributorByLogin(login: String): Option[Int] = db withSession {
    for(c <- FindContributorByLogin(login).firstOption) yield c
  }
  
  def findProjectByPermalink(link: String) = db withSession {
    for(p <- FindProjectByPermaLink(link).firstOption) 
      yield Project(p._1,p._3)
  }
  
  def createContributor(c: Contributor){
    db withSession {
      Contributors.insert(0, c.name, c.login, c.avatar.getOrElse(null), c.style)
    }
  }
  
  def setup_!() = db withSession {
    (ScalaVersions.ddl ++ 
     ProjectScalaVersions.ddl ++ 
     Projects.ddl ++ 
     ProjectVersions.ddl ++ 
     ProjectTags.ddl ++ 
     Tags.ddl ++ 
     Contributors.ddl) create
  }
}

trait Connection {
  lazy val db = Database.forURL(
    Props.get("db.url").openOr("jdbc:h2:database/temp"),
    driver = Props.get("db.driver").openOr("org.h2.Driver"),
    user = Props.get("db.user").openOr(""),
    password = Props.get("db.password").openOr(""))
}

trait Queries { _: Tables with Connection =>
   
   /** lists for projects **/
   
   lazy val ListTopFiveProjects = for {
     p <- Projects
     c <- p.contributor
     _ <- Query orderBy p.addedAt.desc
     _ <- Query take 5
   } yield p.id ~ p.name ~ p.description ~ p.permalink ~ p.addedAt ~ c.name ~ c.login ~ c.avatarUrl
   
   lazy val ListTagsForProject = for {
     id <- Parameters[Int]
     pt <- ProjectTags if pt.projectId === id
     t <- Tags if t.id === pt.tagId
   } yield t.id ~ t.name
   
   lazy val ListScalaVersionsForProject = for {
     id <- Parameters[Int]
     psv <- ProjectScalaVersions if psv.projectId === id
     s <- ScalaVersions if s.id === psv.scalaVersionId
   } yield s.id ~ s.version ~ s.releaseType
   
   /** Listers **/
   
   lazy val ListScalaVersions = for {
     v <- ScalaVersions
   } yield v.*
   
   lazy val ListAllTags = for {
     t <- Tags
   } yield t.id ~ t.name
   
   /** Finders **/
   
   lazy val FindProjectByPermaLink = for {
     l <- Parameters[String]
     p <- Projects if p.permalink === l
   } yield p.*
   
   lazy val FindContributorByLogin = for {
     login <- Parameters[String]
     c <- Contributors if c.login === login
   } yield c.id
}
