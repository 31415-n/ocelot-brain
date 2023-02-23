name := "ocelot-brain"

// do not forget to change the version in `Ocelot.scala`
version := "0.9.1"

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  "org.apache.logging.log4j" % "log4j-core" % "2.19.0",
  "org.apache.logging.log4j" % "log4j-api" % "2.19.0",
  "com.google.guava" % "guava" % "28.1-jre",
  "commons-codec" % "commons-codec" % "1.15",
  "com.typesafe" % "config" % "1.4.2",
  "org.apache.commons" % "commons-lang3" % "3.12.0",
  "org.apache.commons" % "commons-text" % "1.9",
  "commons-io" % "commons-io" % "2.11.0",
  "org.ow2.asm" % "asm" % "9.3"
)

assemblyJarName := s"ocelot-brain-${version.value}.jar"

Global / fileInputExcludeFilter := NothingFilter.toNio
