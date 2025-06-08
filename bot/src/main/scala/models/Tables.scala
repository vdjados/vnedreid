package models

import slick.jdbc.PostgresProfile.api._
import java.time.Instant

// Таблица для хранения настроек чата
class ChatSettings(tag: Tag) extends Table[(Long, Int, Int)](tag, "chat_settings") {
  def chatId = column[Long]("chat_id", O.PrimaryKey)
  def messageWindow = column[Int]("message_window")
  def maxMessages = column[Int]("max_messages")
  
  def * = (chatId, messageWindow, maxMessages)
}

// Таблица для хранения статистики спама
class SpamStats(tag: Tag) extends Table[(Long, Long, Long, Instant, String)](tag, "spam_stats") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def chatId = column[Long]("chat_id")
  def userId = column[Long]("user_id")
  def timestamp = column[Instant]("timestamp")
  def reason = column[String]("reason")
  
  def * = (id, chatId, userId, timestamp, reason)
}

object Tables {
  val chatSettings = TableQuery[ChatSettings]
  val spamStats = TableQuery[SpamStats]
} 