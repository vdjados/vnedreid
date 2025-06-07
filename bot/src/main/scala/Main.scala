import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

object Main extends App {
  try {
    val botsApi = new TelegramBotsApi(classOf[DefaultBotSession])
    botsApi.registerBot(new SpamBot())
    println("✅ Бот успешно запущен!")
  } catch {
    case e: TelegramApiException =>
      println(s"❌ Ошибка при запуске бота: ${e.getMessage}")
      e.printStackTrace()
  }
}

class SpamBot extends TelegramLongPollingBot {
  override def getBotUsername: String = "MySpamBot"

  override def getBotToken: String = sys.env.getOrElse("BOT_TOKEN", "7671162091:AAFgE04qMLgNVCCmfmOAj2EKbSyBSD2RALI")

  override def onUpdateReceived(update: Update): Unit = {
    if (update.hasMessage && update.getMessage.hasText) {
      val message = update.getMessage
      val chatId = message.getChatId
      val text = message.getText
      val user = message.getFrom

      println(s"""
                 |Recieved message:
                 |Chat ID: $chatId
                 |User: ${user.getUserName} (${user.getFirstName})
                 |Text: $text
                 |""".stripMargin)
      
      
      
    }
  }

}