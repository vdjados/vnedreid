package commands

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.{Message, Update}
import org.telegram.telegrambots.meta.bots.AbsSender

class StartCommand extends CommandHandler {
  override def processCommand(sender: AbsSender, update: Update, response: SendMessage): Unit = {
    val message: Message = update.getMessage
    val userName = Option(message.getFrom.getUserName).getOrElse(message.getFrom.getFirstName)
    
    response.setText(s"""
      |Привет, $userName! 👋
      |
      |Я бот для защиты чатов от спама. Вот что я умею:
      |• Отслеживать подозрительные сообщения
      |• Проверять новых участников
      |• Удалять спам автоматически
      |
      |Чтобы начать использовать меня, добавьте меня в группу и назначьте администратором.
      |
      |Используйте /help для получения списка команд.
      |""".stripMargin)
  }
} 