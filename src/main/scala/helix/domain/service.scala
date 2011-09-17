package helix.domain

import net.liftweb.util.{Props,Helpers}
import java.sql.Timestamp
import helix.db.{MongoRepositories}

object Service extends HelixService with MongoRepositories {
  protected val repository = new MongoRepository
}
