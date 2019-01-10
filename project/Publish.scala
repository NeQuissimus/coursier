
import org.scalajs.sbtplugin.ScalaJSPlugin
import sbt._
import sbt.Keys._

object Publish {

  lazy val dontPublish = Seq(
    publish := {},
    publishLocal := {},
    publishArtifact := false
  )

  def onlyPublishIn(sbv: String) = Seq(
    publishArtifact := {
      val sbv0 = CrossVersion.partialVersion(scalaBinaryVersion.value).fold("") {
        case (maj, min) => s"$maj.$min"
      }
      sbv == sbv0 && publishArtifact.value
    }
  )

  def dontPublishScalaJsIn(sbv: String*) = Seq(
    publishArtifact := {
      (!ScalaJSPlugin.autoImport.isScalaJSProject.value || !sbv.contains(scalaBinaryVersion.value)) &&
        publishArtifact.value
    }
  )

  private def pomStuff = Seq(
    licenses := Seq("Apache 2.0" -> url("http://opensource.org/licenses/Apache-2.0")),
    homepage := Some(url("https://github.com/coursier/coursier")),
    scmInfo := Some(ScmInfo(
      url("https://github.com/coursier/coursier.git"),
      "scm:git:github.com/coursier/coursier.git",
      Some("scm:git:git@github.com:coursier/coursier.git")
    )),
    pomExtra := {
      <developers>
        <developer>
          <id>alexarchambault</id>
          <name>Alexandre Archambault</name>
          <url>https://github.com/alexarchambault</url>
        </developer>
      </developers>
    }
  )

  private def pushToSonatypeStuff = Seq(
    publishMavenStyle := true,
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    credentials ++= {
      Seq("SONATYPE_USER", "SONATYPE_PASS").map(sys.env.get) match {
        case Seq(Some(user), Some(pass)) =>
          Seq(Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass))
        case _ =>
          Seq()
      }
    }
  )

  lazy val released = pomStuff ++ pushToSonatypeStuff ++ Release.settings

}