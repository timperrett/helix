package helix.http.ui

import scala.xml.NodeSeq
import net.liftweb.util.CssSel
import net.liftweb.util.Helpers._
import net.liftweb.textile.TextileParser
import helix.domain._

object DomainBindings {
  class Bind[T](what: T){
    def bind(implicit bind: DataBinding[T]) = bind(what)
  }
  type DataBinding[T] = T => CssSel
  implicit def asCssSelector[T](in : T): Bind[T] = new Bind[T](in)
  
  /**
   * Bind that shit... implicitly! 
   */
  implicit object ProjectBinding extends DataBinding[Project]{
    private val DefaultDescription = <p><strong>No description supplied</strong></p>
    private val DefaultAvatar = "http://en.gravatar.com/unknown"
    
    def apply(project: Project) = (for {
      group <- project.groupId
      artifact <- project.artifactId
    } yield 
      "name" #> project.name &
      "headline" #> project.headline &
      "description" #> project.description.map(TextileParser.toHtml(_)).getOrElse(DefaultDescription) &
      "href=project [href]" #> "/projects/%s/%s".format(group, artifact) &
      "alt=avatar [rel]" #> project.randomContributor.map(_.picture).getOrElse(DefaultAvatar) &
      "alt=activity [src]" #> "/images/activity-%s.png".format(project.activity.toString.toLowerCase) &
      ".activity-text" #> project.activity.toString &
      ".tags *" #> project.tags.map { tag => 
        "a [href]" #> "/tags/%s".format(tag.name) &
        "a *" #> tag.name
      }) getOrElse "invalid ^*" #> NodeSeq.Empty
  }
}
