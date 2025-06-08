package commands

import com.typesafe.scalalogging.LazyLogging
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update}
import org.telegram.telegrambots.meta.bots.AbsSender

trait CommandHandler extends LazyLogging {
  def handleCommand(sender: AbsSender, update: Update): Unit = {
    val message: Message = update.getMessage
    val chatId: Long = message.getChatId
    val text: String = message.getText

    logger.info(s"Received command: $text from chat: $chatId")

    val response = new SendMessage()
    response.setChatId(chatId.toString)
    
    try {
      processCommand(sender, update, response)
    } catch {
      case e: Exception =>
        logger.error(s"Error processing command: ${e.getMessage}", e)
        response.setText("Произошла ошибка при обработке команды. Попробуйте позже.")
    }
    
    sender.execute[Message, SendMessage](response)
  }

  protected def processCommand(sender: AbsSender, update: Update, response: SendMessage): Unit
} 