package spam

import com.typesafe.scalalogging.LazyLogging

class KeywordChecker extends LazyLogging {
  // Списки ключевых слов для разных категорий спама
  private val spamKeywords = Set(
    // Финансовый спам
    "кредит", "займ", "деньги", "ипотека", "кредитная карта", "микрозайм", "быстрый займ",
    "деньги под залог", "деньги до зарплаты", "кредит без справок", "кредит без отказа",
    "кредит с плохой историей", "рефинансирование", "реструктуризация",
    
    // Криптовалютный спам
    "bitcoin", "ethereum", "crypto", "криптовалюта", "блокчейн", "майнинг", "токен",
    "ico", "nft", "defi", "web3", "airdrop", "presale", "whitelist", "staking",
    "mining", "wallet", "exchange", "trading", "invest", "investment",
    
    // Казино и азартные игры
    "казино", "ставки", "беттинг", "bet", "casino", "poker", "рулетка", "слоты",
    "джекпот", "выигрыш", "приз", "бонус", "бездепозитный бонус", "фриспины",
    "gambling", "betting", "sports betting", "live casino",
    
    // Работа и заработок
    "работа", "вакансия", "зарплата", "подработка", "доход", "заработок",
    "удаленная работа", "фриланс", "заработок в интернете", "заработок без вложений",
    "быстрый заработок", "пассивный доход", "инвестиции", "трейдинг",
    "job", "work", "salary", "income", "earn money", "make money",
    
    // Спам-ссылки и реклама
    "купить", "продать", "скидка", "акция", "распродажа", "бесплатно",
    "только сегодня", "ограниченное предложение", "специальная цена",
    "buy", "sell", "discount", "sale", "free", "limited time",
    
    // Подозрительные домены
    "bit.ly", "goo.gl", "tinyurl", "t.co", "ow.ly", "is.gd", "cli.gs",
    "yep.it", "migre.me", "ff.im", "tiny.cc", "url4.eu", "tr.im",
    "twit.ac", "su.pr", "twurl.nl", "snipurl.com", "short.to",
    
    // Спам-слова
    "срочно", "только сейчас", "успейте", "последний шанс", "не упустите",
    "гарантированно", "проверено", "рекомендую", "лучшее предложение",
    "уникальное предложение", "эксклюзивно", "бесплатный", "бесплатно",
    "urgent", "limited time", "don't miss", "guaranteed", "verified",
    
    // Подозрительные символы и паттерны
    "!!!", "???", "$$$", "%%%", "***", "###", "@@@",
    "100%", "1000%", "бесплатно", "бесплатный", "бесплатная",
    "бесплатные", "бесплатное", "бесплатным", "бесплатными",
    
    // Спам-призывы
    "нажмите здесь", "кликните", "подпишитесь", "присоединяйтесь",
    "click here", "subscribe", "join now", "sign up", "register now",
    
    // Спам-обещания
    "заработайте", "получите", "выиграйте", "станьте богатым",
    "earn", "get", "win", "become rich", "make money fast",
    
    // Спам-домены
    ".xyz", ".top", ".win", ".bid", ".loan", ".click", ".work",
    ".download", ".stream", ".review", ".date", ".racing", ".party",
    
    // Спам-слова на английском
    "viagra", "cialis", "pharmacy", "prescription", "medication",
    "weight loss", "diet", "supplement", "vitamin", "health",
    "insurance", "mortgage", "debt", "loan", "credit",
    "make money", "earn money", "work from home", "home business",
    "online business", "mlm", "network marketing", "pyramid scheme"
  )
  
  // Проверка сообщения на наличие ключевых слов
  def checkMessage(text: String): (Boolean, Option[String]) = {
    val lowerText = text.toLowerCase
    val foundKeywords = spamKeywords.filter(keyword => lowerText.contains(keyword.toLowerCase))
    
    if (foundKeywords.nonEmpty) {
      val reason = s"Обнаружены подозрительные слова: ${foundKeywords.mkString(", ")}"
      logger.info(s"Spam detected by keywords: $reason")
      (true, Some(reason))
    } else {
      (false, None)
    }
  }
} 