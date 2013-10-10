name := "soot-infoflow"

organization := "sable"

version := "20131010"

sbtVersion := "0.13"

javacOptions ++= Seq("-source", "1.6")

ideaExcludeFolders += ".idea"

ideaExcludeFolders += ".idea_modules"

resolvers += Resolver.mavenLocal

libraryDependencies += "sable" %% "heros" % "20131010"

libraryDependencies += "sable" %% "soot" % "20131009"

//Temporary - until Scala extensions are split off
libraryDependencies += "soot.plugins" % "soot.plugins.entry-points" % "0.0.1-SNAPSHOT"

//Logging
libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "ch.qos.logback" % "logback-core" % "1.0.13",
  "ch.qos.logback" % "logback-classic" % "1.0.13"
)

libraryDependencies += "junit" % "junit" % "4.11" % "test"

unmanagedSourceDirectories in Compile := Seq(file("src"))

unmanagedSourceDirectories in Test := Seq (file("tests"), file("securibench"))


