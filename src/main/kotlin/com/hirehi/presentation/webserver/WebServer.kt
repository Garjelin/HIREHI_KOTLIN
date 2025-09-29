package com.hirehi.presentation.webserver

import com.hirehi.data.config.DatabaseConfig
import com.hirehi.data.repository.ArchiveRepositoryImpl
import com.hirehi.domain.model.ArchivedJobs
import com.hirehi.domain.model.ArchiveRequest
import com.hirehi.domain.model.ArchiveResponse
import com.hirehi.domain.usecase.ArchiveJobUseCase
import com.hirehi.domain.usecase.GetArchivedJobsUseCase
import com.hirehi.presentation.service.JobService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class WebServer {
    
    private val jobService = JobService()
    private val archiveRepository = ArchiveRepositoryImpl()
    private val archiveJobUseCase = ArchiveJobUseCase(archiveRepository)
    private val getArchivedJobsUseCase = GetArchivedJobsUseCase(archiveRepository)

    fun start(port: Int = 10000) {
        try {
            println("🌐 Запуск веб-сервера на порту $port...")
            
            // Инициализация базы данных
            DatabaseConfig.init()
            initializeDatabase()
            
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
                try {
                    // Загружаем вакансии исключая архивированные
                    val (jobs, statistics) = if (DatabaseConfig.isDatabaseAvailable()) {
                        kotlinx.coroutines.runBlocking {
                            jobService.loadJobsFromJsonExcludingArchived()
                        }
                    } else {
                        jobService.loadJobsFromJson()
                    }
                    
                    // Генерируем HTML с актуальными данными
                    val html = kotlinx.coroutines.runBlocking {
                        jobService.generateHtmlPage(jobs, statistics)
                    }
                    call.respondText(html, ContentType.Text.Html)
                } catch (e: Exception) {
                    println("❌ Ошибка при генерации главной страницы: ${e.message}")
                    // Fallback к статическому файлу
                    val htmlFile = File("jobs_display.html")
                    if (htmlFile.exists()) {
                        call.respondText(htmlFile.readText(), ContentType.Text.Html)
                    } else {
                        call.respondText("HTML файл не найден.", ContentType.Text.Plain)
                    }
                }
            }

            get("/archive") {
                try {
                    val archivedJobs = if (DatabaseConfig.isDatabaseAvailable()) {
                        kotlinx.coroutines.runBlocking {
                            getArchivedJobsUseCase.execute(1000, 0) // Получаем все архивированные вакансии
                        }
                    } else {
                        emptyList()
                    }
                    
                    val archiveHtml = jobService.generateArchivePage(archivedJobs)
                    call.respondText(archiveHtml, ContentType.Text.Html)
                } catch (e: Exception) {
                    println("❌ Ошибка при генерации страницы архива: ${e.message}")
                    call.respondText("Ошибка при загрузке страницы архива", ContentType.Text.Plain)
                }
            }

            get("/api/jobs") {
                val (jobs, statistics) = if (DatabaseConfig.isDatabaseAvailable()) {
                    kotlinx.coroutines.runBlocking {
                        jobService.loadJobsFromJsonExcludingArchived()
                    }
                } else {
                    jobService.loadJobsFromJson()
                }
                call.respond(jobs)
            }

            get("/api/status") {
                val (jobs, statistics) = jobService.loadJobsFromJson()
                val status = mapOf(
                    "status" to "ok",
                    "message" to "HireHi API is running",
                    "jobs_count" to jobs.size,
                    "total_jobs" to (statistics?.totalJobs ?: jobs.size),
                    "filtered_jobs" to (statistics?.filteredJobs ?: jobs.size)
                )
                call.respond(status)
            }

            post("/api/refresh") {
                try {
                    val searchParams = com.hirehi.domain.model.JobSearchParams(
                        keywords = listOf("Kotlin", "Android")
                    )
                            val statistics = kotlinx.coroutines.runBlocking {
                                jobService.loadAndSaveJobs(searchParams)
                            }
                            val (jobs, _) = if (DatabaseConfig.isDatabaseAvailable()) {
                                kotlinx.coroutines.runBlocking {
                                    jobService.loadJobsFromJsonExcludingArchived()
                                }
                            } else {
                                jobService.loadJobsFromJson()
                            }
                            val html = kotlinx.coroutines.runBlocking {
                                jobService.generateHtmlPage(jobs, statistics)
                            }
                            jobService.saveHtmlToFile(html)
                    
                    val response = org.json.JSONObject().apply {
                        put("status", "success")
                        put("message", "Data refreshed successfully")
                        put("total_jobs", statistics.totalJobs)
                        put("filtered_jobs", statistics.filteredJobs)
                        put("last_updated", statistics.lastUpdated)
                    }
                    call.respondText(response.toString(), io.ktor.http.ContentType.Application.Json)
                } catch (e: Exception) {
                    val errorResponse = org.json.JSONObject().apply {
                        put("status", "error")
                        put("message", e.message ?: "Unknown error occurred")
                    }
                    call.response.status(io.ktor.http.HttpStatusCode.InternalServerError)
                    call.respondText(errorResponse.toString(), io.ktor.http.ContentType.Application.Json)
                }
            }

            // Archive endpoints
            post("/api/archive") {
                if (!DatabaseConfig.isDatabaseAvailable()) {
                    call.response.status(HttpStatusCode.ServiceUnavailable)
                    call.respond(mapOf<String, String>("error" to "Database not available"))
                    return@post
                }
                
                try {
                    val request = call.receive<ArchiveRequest>()
                    
                    if (request.jobId.isBlank()) {
                        call.response.status(HttpStatusCode.BadRequest)
                        call.respond(mapOf("error" to "jobId is required"))
                        return@post
                    }
                    
                    // Найти вакансию в текущих данных
                    val (jobs, _) = jobService.loadJobsFromJson()
                    val job = jobs.find { it.id == request.jobId }
                    
                    if (job == null) {
                        call.response.status(HttpStatusCode.NotFound)
                        call.respond(mapOf("error" to "Job not found"))
                        return@post
                    }
                    
                    val success = kotlinx.coroutines.runBlocking {
                        archiveJobUseCase.execute(job, request.reason)
                    }
                    
                    if (success) {
                        call.respond(mapOf("status" to "success", "message" to "Job archived successfully"))
                    } else {
                        call.response.status(HttpStatusCode.InternalServerError)
                        call.respond(mapOf("error" to "Failed to archive job"))
                    }
                } catch (e: Exception) {
                    call.response.status(HttpStatusCode.InternalServerError)
                    call.respond(mapOf<String, String>("error" to (e.message ?: "Unknown error")))
                }
            }

            get("/api/archive") {
                if (!DatabaseConfig.isDatabaseAvailable()) {
                    call.response.status(HttpStatusCode.ServiceUnavailable)
                    call.respond(mapOf<String, String>("error" to "Database not available"))
                    return@get
                }
                
                try {
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                    val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                    
                    val archivedJobs = kotlinx.coroutines.runBlocking {
                        getArchivedJobsUseCase.execute(limit, offset)
                    }
                    
                    call.respond(ArchiveResponse(
                        jobs = archivedJobs,
                        total = archivedJobs.size,
                        limit = limit,
                        offset = offset
                    ))
                } catch (e: Exception) {
                    call.response.status(HttpStatusCode.InternalServerError)
                    call.respond(mapOf<String, String>("error" to (e.message ?: "Unknown error")))
                }
            }

            get("/api/archive/statistics") {
                if (!DatabaseConfig.isDatabaseAvailable()) {
                    call.response.status(HttpStatusCode.ServiceUnavailable)
                    call.respond(mapOf<String, String>("error" to "Database not available"))
                    return@get
                }
                
                try {
                    val statistics = kotlinx.coroutines.runBlocking {
                        archiveRepository.getArchiveStatistics()
                    }
                    call.respond(statistics)
                } catch (e: Exception) {
                    call.response.status(HttpStatusCode.InternalServerError)
                    call.respond(mapOf<String, String>("error" to (e.message ?: "Unknown error")))
                }
            }

            delete("/api/archive/{id}") {
                if (!DatabaseConfig.isDatabaseAvailable()) {
                    call.response.status(HttpStatusCode.ServiceUnavailable)
                    call.respond(mapOf<String, String>("error" to "Database not available"))
                    return@delete
                }
                
                try {
                    val jobId = call.parameters["id"]
                    if (jobId == null) {
                        call.response.status(HttpStatusCode.BadRequest)
                        call.respond(mapOf("error" to "Job ID is required"))
                        return@delete
                    }
                    
                    val success = kotlinx.coroutines.runBlocking {
                        archiveRepository.deleteArchivedJob(jobId)
                    }
                    
                    if (success) {
                        call.respond(mapOf("status" to "success", "message" to "Archived job deleted"))
                    } else {
                        call.response.status(HttpStatusCode.NotFound)
                        call.respond(mapOf("error" to "Archived job not found"))
                    }
                } catch (e: Exception) {
                    call.response.status(HttpStatusCode.InternalServerError)
                    call.respond(mapOf<String, String>("error" to (e.message ?: "Unknown error")))
                }
            }
        }
    }

    private fun initializeDatabase() {
        if (!DatabaseConfig.isDatabaseAvailable()) {
            println("⚠️ База данных недоступна, архивирование отключено")
            return
        }
        
        try {
            transaction {
                SchemaUtils.create(ArchivedJobs)
            }
            println("✅ База данных инициализирована")
        } catch (e: Exception) {
            println("⚠️ Ошибка инициализации базы данных: ${e.message}")
        }
    }

    fun close() {
        jobService.close()
        DatabaseConfig.close()
    }
}
