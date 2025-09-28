package com.hirehi.presentation.webserver

import com.hirehi.presentation.service.JobService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import java.io.File

class WebServer {
    
    private val jobService = JobService()

    fun start(port: Int = 10000) {
        try {
            println("🌐 Запуск веб-сервера на порту $port...")
            
            val server = embeddedServer(Netty, port = port) {
                configureApplication()
            }
            
            // Открываем браузер в отдельном потоке через небольшую задержку
            Thread {
                Thread.sleep(2000) // Ждем 2 секунды, чтобы сервер успел запуститься
                try {
                    val process = ProcessBuilder("xdg-open", "http://localhost:$port").start()
                    println("🌐 Браузер открыт автоматически!")
                } catch (e: Exception) {
                    println("⚠️ Не удалось открыть браузер автоматически. Откройте http://localhost:$port вручную")
                }
            }.start()
            
            println("✅ Веб-сервер запущен! Откройте http://localhost:$port в браузере")
            
            // Запускаем сервер и ждем завершения (блокируем выполнение)
            server.start(wait = true)
            
        } catch (e: Exception) {
            println("❌ Ошибка при запуске веб-сервера: ${e.message}")
            println("⚠️ Попробуйте запустить приложение снова или проверьте, не занят ли порт")
        }
    }

    private fun Application.configureApplication() {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }

        install(CORS) {
            anyHost()
            allowHeader("Content-Type")
            allowMethod(io.ktor.http.HttpMethod.Get)
            allowMethod(io.ktor.http.HttpMethod.Post)
        }

        // Настройка маршрутов
        routing {
            get("/") {
                val htmlFile = File("jobs_display.html")
                if (htmlFile.exists()) {
                    call.respondText(htmlFile.readText(), ContentType.Text.Html)
                } else {
                    call.respondText("HTML файл не найден.", ContentType.Text.Plain)
                }
            }

            get("/api/jobs") {
                val jobs = jobService.loadJobsFromJson()
                call.respond(jobs)
            }

            get("/api/status") {
                val status = mapOf(
                    "status" to "ok",
                    "message" to "HireHi API is running",
                    "jobs_count" to jobService.loadJobsFromJson().size
                )
                call.respond(status)
            }
        }
    }

    fun close() {
        jobService.close()
    }
}
