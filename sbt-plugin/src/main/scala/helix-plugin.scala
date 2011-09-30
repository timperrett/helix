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
  val notesDir = SettingKey[File]("helix-notes-dir", "The directory that contains meta information for helix.")
  val aboutFile = SettingKey[File]("helix-about-file", "The file that contains the description of the project.")
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
    // Here's a few hacky plugin interop things.  We declare some keys that are the same as other plugin's keys.  Then we optionally use them if they're defined.
    private[this] val posterousAboutFile = SettingKey[File]("about-file")
    private[this] val gitRemoteRepo = SettingKey[String]("git-remote-repo", "The remote git repository assoicated with this project") 

    val settings = Seq(
      HelixKeys.helixLocation := "helix.scala-lang.org",
      HelixKeys.notesDir <<= baseDirectory / "notes",
      // Use posterous file if configured, otherwise use notes directory.
      HelixKeys.aboutFile <<= posterousAboutFile or (HelixKeys.notesDir / "about.markdown"),
      // We might even be able to grab the 'headline' from there.
      HelixKeys.name <<= name.identity,
      HelixKeys.artifactId <<= name.identity,
      HelixKeys.groupId <<= organization.identity,
      HelixKeys.repository <<= publishTo apply { 
        case Some(MavenRepository(_, url))  => url
        // TODO - Support other types of repos.
        case e                              => error("Unknown publishing repository: " + e)
      },
      HelixKeys.description <<= HelixKeys.aboutFile map slurpFile,
      // TODO - Don't bomb if this is not defined!
      HelixKeys.githubUrl <<= extractGithubFromGit,
      HelixKeys.tags <<= HelixKeys.tags ?? Seq(),
      // TODO - Pull githubUrl from configured gitRemoteUrl if one exists.
      HelixKeys.projectInfo <<= (HelixKeys.name, HelixKeys.headline, HelixKeys.description, HelixKeys.githubUrl, 
                                 HelixKeys.tags, HelixKeys.groupId, HelixKeys.artifactId, HelixKeys.repository) map HelixProjectInfo.apply,
      HelixKeys.addProject <<= (HelixKeys.helixLocation, HelixKeys.projectInfo, streams) map { (loc, info, s) =>
        s.log.info("Would have pushed: " + info + " to " + loc)
      }
    )
    /** Attempts to extract the project location from a git remote uri. */
    private[this] def extractGithubFromGit = gitRemoteRepo apply { repo =>
      val Http = new util.matching.Regex("https://github.com/(\\S+)/(\\S+)\\.git")
      val Git  = new util.matching.Regex("git://github.com/(\\S+)/(\\S+)\\.git")
      val Git2 = new util.matching.Regex("git@github.com:(\\S+)/(\\S+)\\.git")
      def makeGitHubUrl(user: String, project: String) = "https://github.com/"+user+"/"+project
      repo match {
        case Http(user, project) => makeGitHubUrl(user, project)
        case Git(user, project)  => makeGitHubUrl(user, project)
        case Git2(user, project) => makeGitHubUrl(user, project)
        case _                   => error("Could not extract github url from git-remote-repository key.   Please specify helix-github-url in your project.")
      }
    }
    /** Slurps an entire file into a string, for good luck. */
    private[this] def slurpFile(f: File): String = 
      IO.reader(f) { reader =>
        val ls = System getProperty "line.separator"
        @annotation.tailrec def read(sb: StringBuilder): String =
          reader.readLine match {
            case null => sb.toString
            case line => 
              sb.append(line)
              sb.append(ls)
              read(sb)
          }
        read(new StringBuilder)
      }
  }
}
