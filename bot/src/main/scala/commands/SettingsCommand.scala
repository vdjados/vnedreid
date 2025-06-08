package commands

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update}
import org.telegram.telegrambots.meta.bots.AbsSender
import com.typesafe.scalalogging.LazyLogging

class SettingsCommand extends CommandHandler with LazyLogging {
  override def processCommand(sender: AbsSender, update: Update, response: SendMessage): Unit = {
    val message: Message = update.getMessage
    val chatId = message.getChatId
    
    // TODO: Проверка прав администратора
    
    response.setText("""
      |⚙️ Настройки антиспама
      |
      |Доступные настройки:
      |• Время задержки перед удалением
      |• Список спам-слов
      |• Список разрешенных доменов
      |• Настройки проверки капса
      |
      |Используйте команды:
      |/settings delay <секунды> - установить задержку
      |/settings spamwords - управление списком спам-слов
      |/settings domains - управление списком доменов
      |/settings caps - настройки проверки капса
      |
      |Пример: /settings delay 5
      |""".stripMargin)
  }
} 