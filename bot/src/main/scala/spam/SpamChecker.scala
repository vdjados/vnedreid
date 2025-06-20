package spam

import com.typesafe.scalalogging.LazyLogging
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update}
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import spray.json._
import DefaultJsonProtocol._

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.Duration

// Определяем формат для JSON
object MessageJsonProtocol extends DefaultJsonProtocol {
  implicit val messageFormat = jsonFormat6(MessageData)
}

case class MessageData(
  text: String,
  username: String,
  first_name: String,
  last_name: String,
  is_deleted: Int,
  has_media: Int
)

class SpamChecker extends LazyLogging {
  private val client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(5))
    .build()
    
  // Порог уверенности для удаления сообщения
  private val CONFIDENCE_THRESHOLD = 0.8

  def checkMessage(message: Message, bot: TelegramLongPollingBot): Unit = {
    val user = message.getFrom
    val messageData = MessageData(
      text = message.getText,
      username = Option(user.getUserName).getOrElse(""),
      first_name = Option(user.getFirstName).getOrElse(""),
      last_name = Option(user.getLastName).getOrElse(""),
      is_deleted = -1,
      has_media = if (message.hasPhoto || message.hasVideo || message.hasDocument) 1 else 0
    )

    import MessageJsonProtocol._
    val requestBody = messageData.toJson.compactPrint

    logger.info(s"Sending message data to ML service: $requestBody")

    try {
      val request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8000/check_spam"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build()

      val response = client.send(request, HttpResponse.BodyHandlers.ofString())
      
      if (response.statusCode() == 200) {
        val responseJson = response.body().parseJson.asJsObject
        val isSpam = responseJson.fields("is_spam").convertTo[Boolean]
        val confidence = responseJson.fields("confidence").convertTo[Double]
        
        logger.info(s"""
          |Received response from ML service:
          |Status: ${response.statusCode()}
          |Is spam: $isSpam
          |Confidence: $confidence
          |Threshold: $CONFIDENCE_THRESHOLD
          |Full response: ${response.body()}
          |""".stripMargin)
        
        if (isSpam && confidence >= CONFIDENCE_THRESHOLD) {
          logger.info(s"Message marked as spam with confidence $confidence (threshold: $CONFIDENCE_THRESHOLD)")
          handleSpamCheckResult(message, bot, confidence)
        } else if (isSpam) {
          logger.info(s"Message marked as spam but confidence $confidence is below threshold $CONFIDENCE_THRESHOLD - keeping message")
        }
      } else {
        logger.error(s"ML service returned error status: ${response.statusCode()}")
        logger.error(s"Error response: ${response.body()}")
      }
    } catch {
      case e: Exception =>
        logger.error(s"Error checking message with ML service: ${e.getMessage}", e)
    }
  }

  private def handleSpamCheckResult(message: Message, bot: TelegramLongPollingBot, confidence: Double): Unit = {
    try {
      // Отправляем предупреждение в чат
      val warning = new SendMessage()
      warning.setChatId(message.getChatId.toString)
      warning.setText(s"""
        |⚠️ Внимание! Обнаружено подозрительное сообщение от ${message.getFrom.getUserName}:
        |
        |Вероятность спама: ${(confidence * 100).toInt}%
        |""".stripMargin)
      
      bot.execute[Message, SendMessage](warning)
      
      // Удаляем сообщение
      val deleteMessage = new DeleteMessage()
      deleteMessage.setChatId(message.getChatId.toString)
      deleteMessage.setMessageId(message.getMessageId)
      bot.execute[java.lang.Boolean, DeleteMessage](deleteMessage)
      
      logger.info(s"Successfully deleted spam message from ${message.getFrom.getUserName}")
    } catch {
      case e: TelegramApiException =>
        logger.error(s"Error handling spam message: ${e.getMessage}", e)
    }
  }
}

case class SpamCheckResult(isSpam: Boolean, reason: String) 