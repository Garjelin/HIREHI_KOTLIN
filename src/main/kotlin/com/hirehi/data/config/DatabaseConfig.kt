package com.hirehi.data.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.sql.Database
import java.net.URI

object DatabaseConfig {
    private var dataSource: HikariDataSource? = null
    private var isInitialized = false
    
    fun init() {
        // Загружаем переменные окружения из .env файла для локальной разработки
        val dotenv = try {
            dotenv {
                directory = "./"
                filename = "local.env"
                ignoreIfMissing = true
            }
        } catch (e: Exception) {
            println("⚠️ Не удалось загрузить local.env файл: ${e.message}")
            null
        }
        
        // Получаем DATABASE_URL из переменных окружения или .env файла
        val databaseUrl = System.getenv("DATABASE_URL") ?: dotenv?.get("DATABASE_URL")
        
        // Если нет DATABASE_URL, пропускаем инициализацию БД
        if (databaseUrl == null) {
            println("⚠️ DATABASE_URL не установлен, работаем без базы данных")
            return
        }
        
        val config = HikariConfig().apply {
            // Parse DATABASE_URL if it's a full URL (for production)
            if (databaseUrl.startsWith("postgresql://")) {
                val uri = URI(databaseUrl)
                jdbcUrl = "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}"
                username = uri.userInfo?.split(":")?.get(0)
                password = uri.userInfo?.split(":")?.get(1)
            } else {
                // For local development
                jdbcUrl = databaseUrl
                username = System.getenv("DB_USERNAME") ?: "postgres"
                password = System.getenv("DB_PASSWORD") ?: "password"
            }
            
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            minimumIdle = 1
            connectionTimeout = 30000
            idleTimeout = 600000
            maxLifetime = 1800000
        }
        
        dataSource = HikariDataSource(config)
        Database.connect(dataSource!!)
        isInitialized = true
        println("✅ База данных подключена")
    }
    
    fun isDatabaseAvailable(): Boolean {
        return isInitialized && dataSource != null
    }
    
    fun close() {
        dataSource?.close()
    }
}
