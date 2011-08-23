package helix.db

// import org.scalaquery.session._
// import org.scalaquery.session.Database.threadLocalSession
// import org.scalaquery.ql._
// import org.scalaquery.ql.TypeMapper._
// import org.scalaquery.ql.extended.MySQLDriver.Implicit._
// import org.scalaquery.ql.extended.{ExtendedTable => Table}
import net.liftweb.util.{Props,Helpers}
import java.sql.Timestamp
import helix.domain._

object Storage extends MongoRepositories with HelixService {
  protected val repository = new MongoRepository
}
