name := "ocelot-brain"

// do not forget to change the version in `Ocelot.scala`
version := "0.3.2"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "org.apache.logging.log4j" % "log4j-core" % "2.12.0",
  "org.apache.logging.log4j" % "log4j-api" % "2.12.0",
  "com.google.guava" % "guava" % "28.0-jre",
  "commons-codec" % "commons-codec" % "1.13",
  "com.typesafe" % "config" % "1.3.4",
  "org.apache.commons" % "commons-lang3" % "3.9",
  "org.apache.commons" % "commons-text" % "1.7",
  "commons-io" % "commons-io" % "2.6",
  "org.ow2.asm" % "asm" % "7.1"
)

assemblyJarName := s"ocelot-brain-${version.value}.jar"
