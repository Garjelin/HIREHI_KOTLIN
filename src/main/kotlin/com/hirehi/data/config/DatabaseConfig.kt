package com.hirehi.data.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import java.net.URI

object DatabaseConfig {
    private var dataSource: HikariDataSource? = null
    
    fun init() {
        val databaseUrl = System.getenv("DATABASE_URL") ?: "postgresql://localhost:5432/hirehi_dev"
        
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
    }
    
    fun close() {
        dataSource?.close()
    }
}
