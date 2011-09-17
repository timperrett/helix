// package helix.db
// 
// import net.liftweb.common.Full
// import net.liftweb.util.Props
// import com.novus.salat._
// import com.novus.salat.global._
// import com.novus.salat.dao._
// import com.mongodb.casbah.Imports._
// import com.mongodb.casbah.MongoURI
// import com.novus.salat.annotations.{Key,Salat}
// 
// case class Thing(
//   @Key("_id") id: ObjectId = new ObjectId,
//   what: Map[String, List[String]]
// )
// 
// 
// object Sandbox {
//   val mongo: MongoDB = {
//     val db = MongoConnection(
//       Props.get("mongo.host").openOr("localhost"), 
//       Props.get("mongo.port").map(_.toInt).openOr(27017))(
//       Props.get("mongo.db").openOr("helix")
//     )
// 
//     // if the env specifies username and password, try to use
//     // them, otherwise, just try to connect without auth.
//     (Props.get("mongo.username"), Props.get("mongo.password")) match {
//       case (Full(username), Full(password)) => 
//         if(db.authenticate(username, password)) db
//         else throw new IllegalArgumentException("Inavlid username and/or password")
//       case _ => db
//     }
//   }
//   
//   def example() = ThingDAO.insert(Thing(what = Map("123" -> List("Thing", "Other"))))
//   
//   import com.novus.salat.annotations.{Key,Salat}
// 
//   object ThingDAO extends SalatDAO[Thing, ObjectId](
//     collection = mongo("things"))
// }
// 
