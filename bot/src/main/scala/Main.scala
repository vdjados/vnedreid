import com.typesafe.scalalogging.LazyLogging
import config.BotConfig
import commands.{StartCommand, HelpCommand}
import spam.{SpamChecker, SpamCheckResult}
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update}
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

object Main extends App with LazyLogging {
  try {
    val botsApi = new TelegramBotsApi(classOf[DefaultBotSession])
    botsApi.registerBot(new SpamBot())
    logger.info("✅ Бот успешно запущен!")
  } catch {
    case e: TelegramApiException =>
      logger.error(s"❌ Ошибка при запуске бота: ${e.getMessage}", e)
  }
}

class SpamBot extends TelegramLongPollingBot with LazyLogging {
  private val startCommand = new StartCommand()
  private val helpCommand = new HelpCommand()
  private val spamChecker = new SpamChecker()

  override def getBotUsername: String = BotConfig.botUsername

  override def getBotToken: String = BotConfig.botToken

  override def onUpdateReceived(update: Update): Unit = {
    if (update.hasMessage && update.getMessage.hasText) {
      val message = update.getMessage
      val chatId = message.getChatId
      val text = message.getText
      val user = message.getFrom

      logger.info(s"""
        |Received message:
        |Chat ID: $chatId
        |User: ${user.getUserName} (${user.getFirstName})
        |Text: $text
        |""".stripMargin)

      // Обработка команд
      text match {
        case "/start" => startCommand.handleCommand(this, update)
        case "/help" => helpCommand.handleCommand(this, update)
        case _ => checkForSpam(message)
      }
    }
  }

  private def checkForSpam(message: Message): Unit = {
    val checkResult = spamChecker.checkMessage(message)
    
    if (checkResult.isSpam) {
      // Отправляем предупреждение в чат
      val warning = new SendMessage()
      warning.setChatId(message.getChatId.toString)
      warning.setText(s"""
        |⚠️ Внимание! Обнаружено подозрительное сообщение от ${message.getFrom.getUserName}:
        |
        |${checkResult.reason}
        |
        |Сообщение будет удалено через 5 секунд.
        |""".stripMargin)
      
      execute[Message, SendMessage](warning)
      
      // TODO: Добавить удаление сообщения через 5 секунд
      // Для этого нужно будет использовать Telegram API для удаления сообщений
    }
  }
}