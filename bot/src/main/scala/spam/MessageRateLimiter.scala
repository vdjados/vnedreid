package spam

import scala.collection.mutable
import java.time.Instant

class MessageRateLimiter {
  // Хранит информацию о последних сообщениях пользователя
  private case class UserMessageInfo(
    lastMessageTime: Instant,
    messageCount: Int
  )
  
  private val userMessages = mutable.Map[Long, UserMessageInfo]()
  
  // Настройки для определения спама
  private var messageWindowSeconds = 10 // Окно времени для подсчета сообщений
  private var maxMessagesPerWindow = 5 // Максимальное количество сообщений в окне
  
  def checkMessageRate(userId: Long): Boolean = {
    val now = Instant.now()
    val userInfo = userMessages.getOrElse(userId, UserMessageInfo(now, 0))
    
    // Если прошло больше времени чем окно, сбрасываем счетчик
    if (now.getEpochSecond - userInfo.lastMessageTime.getEpochSecond > messageWindowSeconds) {
      userMessages(userId) = UserMessageInfo(now, 1)
      false
    } else {
      // Увеличиваем счетчик сообщений
      val newCount = userInfo.messageCount + 1
      userMessages(userId) = UserMessageInfo(userInfo.lastMessageTime, newCount)
      
      // Проверяем, не превышен ли лимит
      newCount > maxMessagesPerWindow
    }
  }
  
  def resetUser(userId: Long): Unit = {
    userMessages.remove(userId)
  }
  
  // Методы для настройки параметров
  def setMessageWindow(seconds: Int): Unit = {
    require(seconds > 0, "Window size must be positive")
    messageWindowSeconds = seconds
  }
  
  def setMaxMessages(max: Int): Unit = {
    require(max > 0, "Maximum messages must be positive")
    maxMessagesPerWindow = max
  }
  
  def getSettings: (Int, Int) = (messageWindowSeconds, maxMessagesPerWindow)
} 