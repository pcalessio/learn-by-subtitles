import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

object LearnBySubtitlesAppBuild extends Build {
  val Organization = "com.github.fedeoasi"
  val Name = "Learn By Subtitles App"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.10.0"
  val ScalatraVersion = "2.2.0"

  lazy val project = Project (
    "learn-by-subtitles-app",
    file("."),
    settings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases + "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      libraryDependencies ++= Seq(
        "org.apache.xmlrpc" % "xmlrpc" % "3.1.3",
        "org.apache.xmlrpc" % "xmlrpc-client" % "3.1.3",
        "org.apache.commons" % "commons-io" % "1.3.2",
        "commons-lang" % "commons-lang" % "2.6",
        "com.typesafe.slick" %% "slick" % "1.0.0",
        "org.slf4j" % "slf4j-nop" % "1.6.4",
        "org.elasticsearch" % "elasticsearch" % "0.90.1",
        "com.h2database" % "h2" % "1.3.166",
        "com.google.guava" % "guava" % "14.0.1",
        "mysql" % "mysql-connector-java" % "5.1.24",
        "org.json4s" %% "json4s-jackson" % "3.2.4",
        "org.codehaus.jackson" % "jackson-core-asl" % "1.6.1",
        "net.databinder.dispatch" %% "dispatch-core" % "0.10.1",
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "org.scalatra" %% "scalatra-scalatest" % "2.2.1" % "test",
        "org.scalatra" %% "scalatra-commands" % "2.2.1",
        "ch.qos.logback" % "logback-classic" % "1.0.6" % "runtime",
        "org.eclipse.jetty" % "jetty-webapp" % "8.1.8.v20121106" % "container",
        "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
      ),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ),  /* add extra bindings here */
            Some("templates")
          )
        )
      }
    )
  )
}
