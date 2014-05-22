name := "pcodec"

version := "1.0"

scalaVersion := "2.10.4"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "org.typelevel" %% "scodec-stream" % "1.0.0-SNAPSHOT",
  "org.apache.kafka" % "kafka_2.10" % "0.8.1"
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri")
)
    