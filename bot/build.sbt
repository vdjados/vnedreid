name := "telegram-spam-bot"
version := "0.1"
scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "org.telegram" % "telegrambots" % "6.9.0",
  "org.telegram" % "telegrambots-abilities" % "6.9.0",
  "org.postgresql" % "postgresql" % "42.6.0",
  "io.getquill" %% "quill-jdbc" % "4.6.0"
)