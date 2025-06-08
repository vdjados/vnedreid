package db

import models.Tables
import slick.jdbc.PostgresProfile.api._
import java.time.Instant
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import com.typesafe.scalalogging.LazyLogging

class DatabaseManager extends LazyLogging {
  // Конфигурация подключения к базе данных
  private val db = Database.forConfig("db")
  
  // Создание таблиц при старте
  def init(): Unit = {
    try {
      logger.info("Initializing database...")
      val setup = DBIO.seq(
        Tables.chatSettings.schema.createIfNotExists,
        Tables.spamStats.schema.createIfNotExists
      )
      
      // Увеличим таймаут до 30 секунд
      Await.result(db.run(setup), 30.seconds)
      logger.info("Database tables created successfully")
      
      // Проверяем подключение
      val testQuery = sql"SELECT 1".as[Int]
      val result = Await.result(db.run(testQuery), 5.seconds)
      logger.info(s"Database connection test successful: $result")
      
    } catch {
      case e: Exception =>
        logger.error(s"Error initializing database: ${e.getMessage}", e)
        throw e // Пробрасываем исключение дальше, чтобы бот не запустился с неработающей БД
    }
  }
  
  // Получение настроек чата
  def getChatSettings(chatId: Long): Future[Option[(Int, Int)]] = {
    logger.debug(s"Getting settings for chat $chatId")
    db.run(
      Tables.chatSettings
        .filter(_.chatId === chatId)
        .map(s => (s.messageWindow, s.maxMessages))
        .result
        .headOption
    )
  }
  
  // Сохранение настроек чата
  def saveChatSettings(chatId: Long, messageWindow: Int, maxMessages: Int): Future[Int] = {
    logger.debug(s"Saving settings for chat $chatId: window=$messageWindow, max=$maxMessages")
    db.run(
      Tables.chatSettings.insertOrUpdate((chatId, messageWindow, maxMessages))
    )
  }
  
  // Сохранение статистики спама
  def logSpamEvent(chatId: Long, userId: Long, reason: String): Future[Long] = {
    logger.debug(s"Logging spam event: chat=$chatId, user=$userId, reason=$reason")
    db.run(
      (Tables.spamStats returning Tables.spamStats.map(_.id)) +=
        (0L, chatId, userId, Instant.now(), reason)
    )
  }
  
  // Получение статистики спама для чата
  def getSpamStats(chatId: Long, limit: Int = 10): Future[Seq[(Long, Instant, String)]] = {
    logger.debug(s"Getting spam stats for chat $chatId, limit=$limit")
    db.run(
      Tables.spamStats
        .filter(_.chatId === chatId)
        .sortBy(_.timestamp.desc)
        .take(limit)
        .map(s => (s.userId, s.timestamp, s.reason))
        .result
    )
  }
  
  // Закрытие соединения с базой данных
  def close(): Unit = {
    logger.info("Closing database connection")
    db.close()
  }
} 