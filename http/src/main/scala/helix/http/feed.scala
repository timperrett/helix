package helix.http

import net.liftweb.util.NamedPF
// import net.liftweb.http.{Req,GetRequst,LiftRules}

/**
 * The goal with the feed is to create an implicit.ly style 
 * feed of new projects and their version updates.
 */
// object Feed extends Dispatcher {
//   override def dispatch = {
//     val feed = LiftRules.DispatchPF = NamedPF("Atom Feed"){
//       case Req("project" :: group :: artifact :: Nil, "rss", GetRequst) => 
//     }
//     super.dispatch ::: List(feed)
//   }
// }