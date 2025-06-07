package config

import com.typesafe.config.{Config, ConfigFactory}

object BotConfig {
  private val config: Config = ConfigFactory.load()
  
  val botUsername: String = config.getString("bot.username")
  val botToken: String = if (config.hasPath("bot.token")) config.getString("bot.token") else config.getString("bot.default-token")
  
  object Database {
    val url: String = config.getString("database.url")
    val user: String = if (config.hasPath("database.user")) config.getString("database.user") else config.getString("database.default-user")
    val password: String = if (config.hasPath("database.password")) config.getString("database.password") else config.getString("database.default-password")
  }
} 