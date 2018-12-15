name := "ocelot-brain"

// do not forget to change the version in `Ocelot.scala`
version := "0.2.4"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "org.apache.logging.log4j" % "log4j-core" % "2.11.1",
  "org.apache.logging.log4j" % "log4j-api" % "2.11.1",
  "com.google.guava" % "guava" % "26.0-jre",
  "commons-codec" % "commons-codec" % "1.11",
  "com.typesafe" % "config" % "1.3.3",
  "org.apache.commons" % "commons-lang3" % "3.8",
  "org.apache.commons" % "commons-text" % "1.4",
  "commons-io" % "commons-io" % "2.6",
  "org.ow2.asm" % "asm" % "6.2.1"
)

assemblyJarName := s"ocelot-brain-${version.value}.jar"
