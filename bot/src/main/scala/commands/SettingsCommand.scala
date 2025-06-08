package commands

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update}
import org.telegram.telegrambots.meta.bots.AbsSender
import com.typesafe.scalalogging.LazyLogging
import spam.SpamChecker

class SettingsCommand extends CommandHandler with LazyLogging {
  private val spamChecker = new SpamChecker()
  
  override def processCommand(sender: AbsSender, update: Update, response: SendMessage): Unit = {
    val message: Message = update.getMessage
    val chatId = message.getChatId
    val text = message.getText
    
    // TODO: Проверка прав администратора
    
    val args = text.split("\\s+").drop(1) // Убираем команду /settings
    
    if (args.isEmpty) {
      // Показываем текущие настройки
      showCurrentSettings(response, chatId)
    } else {
      args(0) match {
        case "window" if args.length > 1 =>
          try {
            val seconds = args(1).toInt
            spamChecker.setMessageWindow(chatId, seconds)
            response.setText(s"✅ Окно времени установлено на $seconds секунд")
          } catch {
            case _: NumberFormatException =>
              response.setText("❌ Неверный формат числа. Используйте целое положительное число.")
          }
          
        case "messages" if args.length > 1 =>
          try {
            val max = args(1).toInt
            spamChecker.setMaxMessages(chatId, max)
            response.setText(s"✅ Максимальное количество сообщений установлено на $max")
          } catch {
            case _: NumberFormatException =>
              response.setText("❌ Неверный формат числа. Используйте целое положительное число.")
          }
          
        case "stats" =>
          spamChecker.getSpamStats(chatId)
          response.setText("📊 Статистика спама будет отправлена в следующем сообщении")
          
        case _ =>
          response.setText("""
            |⚙️ Настройки антиспама
            |
            |Доступные команды:
            |/settings window <секунды> - установить размер окна времени
            |/settings messages <количество> - установить максимальное количество сообщений
            |/settings stats - показать статистику спама
            |
            |Примеры:
            |/settings window 15 - установить окно в 15 секунд
            |/settings messages 3 - установить лимит в 3 сообщения
            |""".stripMargin)
      }
    }
  }
  
  private def showCurrentSettings(response: SendMessage, chatId: Long): Unit = {
    val (window, max) = spamChecker.getSettings
    response.setText(s"""
      |⚙️ Текущие настройки антиспама:
      |
      |• Окно времени: $window секунд
      |• Максимум сообщений: $max
      |
      |Используйте команды:
      |/settings window <секунды> - изменить окно времени
      |/settings messages <количество> - изменить лимит сообщений
      |/settings stats - показать статистику спама
      |""".stripMargin)
  }
} 