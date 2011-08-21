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

trait Tables {
  lazy val Projects = new Table[(Int, Int, String, String, String, String, String, String, String, Timestamp)]("projects"){
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def contributorId = column[Int]("contributor_id")
    def name = column[String]("name", O.NotNull)
    def description = column[String]("description")
    def permalink = column[String]("permalink")
    def groupId = column[String]("group_id")
    def artifactId = column[String]("artifact_id")
    def usagePhase = column[String]("usage_phase")
    def sourceURL = column[String]("source_url")
    def addedAt = column[Timestamp]("added_at", O.Default(new Timestamp(Helpers.millis)))
    // fks
    def contributor = foreignKey("contributor_id_fk", contributorId, Contributors)(_.id)
    def * = id ~ contributorId ~ name ~ description ~ permalink ~ groupId ~ artifactId ~ usagePhase ~ sourceURL ~ addedAt
  }
  
  lazy val ScalaVersions = new Table[(Int,String,ReleaseType)]("scala_versions"){
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def version = column[String]("version", O.NotNull)
    def releaseType = column[ReleaseType]("release_type")
    def * = id ~ version ~ releaseType
  }
  
  lazy val ProjectScalaVersions = new Table[(Int,Int,Int)]("project_scala_versions"){
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def projectId = column[Int]("project_id", O.NotNull)
    def scalaVersionId = column[Int]("scala_version_id", O.NotNull)
    // fks
    def project = foreignKey("project_fk", projectId, Projects)(_.id)
    def scalaVersion = foreignKey("scala_version_fk", scalaVersionId, ScalaVersions)(_.id)
    // projection
    def * = id ~ projectId ~ scalaVersionId
  }
  
  lazy val ProjectTags = new Table[(Int,Int,Int)]("project_tags"){
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def projectId = column[Int]("project_id")
    def tagId = column[Int]("tag_id")
    // fks
    def project = foreignKey("project_fk", projectId, Projects)(_.id)
    def tags = foreignKey("tag_fk", tagId, Tags)(_.id)
    // projection
    def * = id ~ projectId ~ tagId
  }
  
  lazy val Tags = new Table[(Int,String)]("tags"){
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def * = id ~ name
  }
  
  lazy val ProjectVersions = new Table[(Int,Int,Int,String)]("project_versions"){
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def projectId = column[Int]("project_id")
    def scalaVersionId = column[Int]("scala_version_id")
    def version = column[String]("version", O.NotNull)
    // fks
    def scalaVersion = foreignKey("scala_version_fk", scalaVersionId, ScalaVersions)(_.id)
    def project = foreignKey("project_fk", projectId, Projects)(_.id)
    // projection 
    def * = id ~ projectId ~ scalaVersionId ~ version
  }
  
  lazy val Contributors = new Table[(Int,String,String,String,String)]("contributors"){
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def login = column[String]("login", O.NotNull)
    def avatarUrl = column[String]("avatar_url")
    def style = column[String]("style")
    def * = id ~ name ~ login ~ avatarUrl ~ style
  }
  
  implicit object ReleaseTypeMapper extends MappedTypeMapper[ReleaseType, String] with BaseTypeMapper[ReleaseType] {
     def map(e: ReleaseType) = e match {
       case Final => "final"
       case RC => "rc"
       case Snapshot => "snapshot"
     }
     def comap(s: String) = s match {
       case "final" => Final
       case "rc" => RC
       case "snapshot" => Snapshot 
     }
     override def sqlTypeName = Some("enum('final', 'rc', 'snapshot')")
  }
}
