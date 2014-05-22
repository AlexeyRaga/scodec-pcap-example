name := "pcodec"

version := "1.0"

scalaVersion := "2.10.4"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "org.typelevel" %% "scodec-stream" % "1.0.0-SNAPSHOT",
  "org.apache.kafka" % "kafka_2.10" % "0.8.0"
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri")
    exclude("org.slf4j", "slf4j-simple"),
  // Logback with slf4j facade
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  "ch.qos.logback" % "logback-core" % "1.0.13",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test"
)
    