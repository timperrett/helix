package helix.db

import net.liftweb.util.{Props,Helpers}
import java.sql.Timestamp
import helix.domain._

object Storage extends MongoRepositories with HelixService {
  protected val repository = new MongoRepository
}
