name := "soot-infoflow"

organization := "sable"

version := "20131004"

scalaVersion := "2.10.2"

javacOptions ++= Seq("-source", "1.6")

retrieveManaged := true

ideaExcludeFolders += ".idea"

ideaExcludeFolders += ".idea_modules"

libraryDependencies += "sable" %% "heros" % "20130920"

libraryDependencies += "sable" %% "soot-javaee" % "20130923"

//Logging
libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "ch.qos.logback" % "logback-core" % "1.0.13",
  "ch.qos.logback" % "logback-classic" % "1.0.13"
)

libraryDependencies += "junit" % "junit" % "4.11"

javacOptions ++= Seq("-source", "1.6")

compileOrder in Compile := CompileOrder.Mixed

unmanagedSourceDirectories in Compile := Seq(file("src"))

unmanagedSourceDirectories in Test := Seq (file("tests"), file("securibench"))


