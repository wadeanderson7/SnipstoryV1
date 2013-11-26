name := "SnipStory"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "mysql" % "mysql-connector-java" % "5.1.25",
  "com.typesafe" %% "play-plugins-mailer" % "2.1.0"
)

play.Project.playJavaSettings

