organization := "com.fedeoasi"

name := "Subtitles Searcher"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.0"

libraryDependencies ++= Seq(
    "org.apache.xmlrpc" % "xmlrpc" % "3.1.3",
    "org.apache.xmlrpc" % "xmlrpc-client" % "3.1.3",
    "org.apache.commons" % "commons-io" % "1.3.2",
    "com.h2database" % "h2" % "1.2.134"
)

testOptions in Test += Tests.Argument("-h","target/html-test-report","-o")
