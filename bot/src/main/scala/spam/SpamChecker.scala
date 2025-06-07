package spam

import com.typesafe.scalalogging.LazyLogging
import org.telegram.telegrambots.meta.api.objects.Message

class SpamChecker extends LazyLogging {
  // Список подозрительных слов и фраз
  private val spamWords = Set(
    "купить", "продать", "заработок", "инвестиции", "криптовалюта",
    "bitcoin", "ethereum", "crypto", "investment", "earn money",
    "работа", "вакансия", "зарплата", "подработка", "доход",
    "казино", "ставки", "беттинг", "bet", "casino",
    "кредит", "займ", "деньги", "кредит", "ипотека"
  )

  // Список подозрительных доменов
  private val spamDomains = Set(
    "bit.ly", "t.me", "telegram.me", "goo.gl", "tinyurl.com",
    "cutt.ly", "is.gd", "v.gd", "ow.ly", "buff.ly"
  )

  def checkMessage(message: Message): SpamCheckResult = {
    val text = message.getText.toLowerCase
    val from = message.getFrom
    val chatId = message.getChatId

    logger.info(s"Checking message from ${from.getUserName} in chat $chatId")

    // Проверяем наличие спам-слов
    val foundSpamWords = spamWords.filter(word => text.contains(word))
    
    // Проверяем наличие ссылок
    val hasLinks = text.contains("http://") || text.contains("https://") || text.contains("t.me/")
    
    // Проверяем наличие подозрительных доменов
    val foundSpamDomains = spamDomains.filter(domain => text.contains(domain))

    // Если найдены спам-слова или подозрительные домены
    if (foundSpamWords.nonEmpty || foundSpamDomains.nonEmpty) {
      logger.warn(s"Spam detected! Words: $foundSpamWords, Domains: $foundSpamDomains")
      SpamCheckResult(
        isSpam = true,
        reason = s"Обнаружены подозрительные слова: ${foundSpamWords.mkString(", ")}" +
                (if (foundSpamDomains.nonEmpty) s"\nПодозрительные домены: ${foundSpamDomains.mkString(", ")}" else "")
      )
    } else {
      SpamCheckResult(isSpam = false, reason = "")
    }
  }
}

case class SpamCheckResult(isSpam: Boolean, reason: String) 