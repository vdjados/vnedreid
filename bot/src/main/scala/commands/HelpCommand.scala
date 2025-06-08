package commands

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update}
import org.telegram.telegrambots.meta.bots.AbsSender

class HelpCommand extends CommandHandler {
  override def processCommand(sender: AbsSender, update: Update, response: SendMessage): Unit = {
    response.setText("""
      |📋 Список доступных команд:
      |
      |/start - Начать работу с ботом
      |/help - Показать это сообщение
      |
      |Команды для администраторов:
      |/settings - Настройки антиспама
      |
      |Для настройки бота:
      |1. Добавьте бота в группу
      |2. Назначьте его администратором
      |3. Используйте /settings для настройки правил
      |
      |По всем вопросам обращайтесь к администратору группы.
      |""".stripMargin)
  }
} 