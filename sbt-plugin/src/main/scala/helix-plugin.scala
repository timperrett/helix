import sbt._
import Build._
import Keys._

object HelixKeys {
  val name = SettingKey[String]("helix-project-name", "The project name displayed in helix.")
  val description = TaskKey[String]("helix-project-description", "The project name displayed in helix.")
  val githubUrl = SettingKey[String]("helix-github-url", "The github URL for the project.")
  val tags = SettingKey[Seq[String]]("helix-project-tags", "The tags to associate with")
  val headline = SettingKey[String]("helix-project-headline", "The headline to display with the project.   A quick description.")
  val groupId = SettingKey[String]("helix-group-id", "The groupId used to publish artifacts for this project.")
  val artifactId = SettingKey[String]("helix-artifact-id", "The artifact used to publish artifacts for this project.")
  val repository = SettingKey[String]("helix-repository-target", "The remote repository where artifacts are deployed for this project.")
  // TODO - Versions and pusing new versions.
  val helixLocation = SettingKey[String]("helix-location", "The URL to helix.")
  val projectInfo = TaskKey[HelixProjectInfo]("helix-project-info", "The URL to helix.")
  val addProject = TaskKey[Unit]("helix-add-project", "Adds this project to the helix index or updates helix information with the current information for this project.")
}

/** This class stores all configuration about a project for helix in one convenient type. */
case class HelixProjectInfo(
  name: String,
  headline: String,
  description: String,
  githubUrl: String,
  tags: Seq[String],
  groupId: String,
  artifactId: String,
  repository: String
)

object HelixPlugin extends Plugin {
  object helix {
    val settings = Seq(
      // TODO - Let's pull description from a file...  Specifically, let's look for it in the posterous about.markdown file.  That makes this a Task.
      // We might even be able to grab the 'headline' from there.
      HelixKeys.name <<= name.identity,
      HelixKeys.artifactId <<= name.identity,
      HelixKeys.groupId <<= organization.identity,
      HelixKeys.repository <<= publishTo apply { 
        case Some(MavenRepository(_, url))  => url
        // TODO - Support other types of repos.
        case _                              => /* Asume maven central */ "http://repo2.maven.org/maven2"
      },
      HelixKeys.tags <<= HelixKeys.tags ?? Seq(),
      // TODO - Pull githubUrl from configured gitRemoteUrl if one exists.
      HelixKeys.projectInfo <<= (HelixKeys.name, HelixKeys.headline, HelixKeys.description, HelixKeys.githubUrl, 
                                 HelixKeys.tags, HelixKeys.groupId, HelixKeys.artifactId, HelixKeys.repository) map HelixProjectInfo.apply,
      HelixKeys.addProject <<= (HelixKeys.helixLocation, HelixKeys.projectInfo, streams) map { (loc, info, s) =>
        s.log.info("Would have pushed: " + info + " to " + loc)
      }
    )
  }
}
