package helix.search

import helix.domain.Project

trait ElasticSearchProvider {
  protected lazy val server = new ElasticSearchServer
}

trait ProjectIndexing { _: ElasticSearchProvider => 
  import org.elasticsearch.common.xcontent.XContentFactory._
  
  def index(project: Project){
    for {
      client <- server.client
      description <- project.description
      group <- project.groupId
      artifact <- project.artifactId
      uid = "%s.%s".format(group, artifact)
    }{
      client.prepareIndex("helix", "project", uid)
        .setSource(jsonBuilder()
          .startObject()
          .field("name", project.name)
          .field("description", description)
          .endObject()
        ).execute().actionGet()
    }
  }
}


import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.node.{Node,NodeBuilder}
import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.loader.YamlSettingsLoader

class ElasticSearchServer {
  import NodeBuilder.nodeBuilder
  import ImmutableSettings.settingsBuilder
  
  private var node: Option[Node] = None
  
  def start(){
    // if the server is not operational, start it
    if(node.isEmpty){
      node = Option(nodeBuilder.client(false).node)
      node.foreach(_.start())
      status()
    }
  }
  
  // TODO: load settings from a yaml on the classpath
  // def settings: ImmutableSettings = {
  //   new YamlSettingsLoader().load(this.getClass.getResourceAsStream())
  //   settingsBuilder.put()
  // }
  
  def stop() = 
    node.foreach(_.close())
  
  def client: Option[Client] = node.map(_.client)
  
  // not 100% what the hell this is for
  private def health: ClusterHealthStatus =
    client.map(_.admin.cluster.prepareHealth().execute.actionGet.getStatus
      ).getOrElse(ClusterHealthStatus.RED)
  
  private def isRedStatus: Boolean = 
    health eq ClusterHealthStatus.RED
  
  def status(){
    for(c <- client){
      if(isRedStatus){
        c.admin.cluster.prepareHealth()
          .setWaitForYellowStatus
          .setTimeout("30s")
          .execute().actionGet()
      }
    }
    if(isRedStatus)
      throw new RuntimeException("ES cluster health status is RED. Server is not able to start.")
  }
}