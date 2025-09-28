package com.hirehi

import com.hirehi.data.local.JobLocalDataSource
import com.hirehi.data.remote.HireHiScraper
import com.hirehi.data.repository.JobRepositoryImpl
import com.hirehi.domain.model.JobSearchParams
import com.hirehi.domain.usecase.GetJobsUseCase
import com.hirehi.domain.usecase.RefreshJobsUseCase
import com.hirehi.presentation.view.SimpleJobView
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.*
import java.io.File
import org.json.JSONObject
import org.json.JSONArray

fun main() {
    runBlocking {
        println("🚀 Запуск HireHi веб-приложения...")
        
        // Сначала запускаем standalone скрипт для получения данных
        println("📡 Получение данных с hirehi.ru...")
        val scraper = HireHiScraperStandalone()
        val keywords = listOf("Kotlin", "Android")
        
        try {
            val allJobs = scraper.getAllJobs()
            if (allJobs.isNotEmpty()) {
                val filteredJobs = scraper.filterJobsByKeywords(allJobs, keywords)
                scraper.saveToJson(filteredJobs, "hirehi_filtered_jobs.json")
                println("✅ Получено ${filteredJobs.size} отфильтрованных вакансий")
            } else {
                println("⚠️ Не удалось получить вакансии, используем mock данные")
            }
        } catch (e: Exception) {
            println("❌ Ошибка при получении данных: ${e.message}")
        } finally {
            scraper.close()
        }
        
        // Теперь запускаем веб-сервер
        println("🌐 Запуск веб-сервера...")
        embeddedServer(Netty, port = getPort()) {
            configureApplication()
        }.start(wait = true)
    }
}

fun Application.configureApplication() {
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

    val jobView = SimpleJobView()

    // Настройка маршрутов
    routing {
        get("/") {
            val jobs = loadJobsFromJson()
            call.respondHtml {
                jobView.renderJobsPage(this, jobs)
            }
        }

        get("/api/jobs") {
            val jobs = loadJobsFromJson()
            call.respond(jobs)
        }

        get("/api/status") {
            val status = mapOf(
                "status" to "ok",
                "message" to "HireHi API is running",
                "jobs_count" to loadJobsFromJson().size
            )
            call.respond(status)
        }
    }
}

private fun loadJobsFromJson(): List<com.hirehi.domain.model.Job> {
    return try {
        val jsonFile = File("hirehi_filtered_jobs.json")
        if (!jsonFile.exists()) {
            println("⚠️ JSON файл не найден, возвращаем пустой список")
            return emptyList()
        }
        
        val jsonText = jsonFile.readText()
        val jsonArray = JSONArray(jsonText)
        val jobs = mutableListOf<com.hirehi.domain.model.Job>()
        
        for (i in 0 until jsonArray.length()) {
            val jobJson = jsonArray.getJSONObject(i)
            val job = parseJobFromJson(jobJson)
            if (job != null) {
                jobs.add(job)
            }
        }
        
        println("📊 Загружено ${jobs.size} вакансий из JSON файла")
        jobs
    } catch (e: Exception) {
        println("❌ Ошибка при загрузке JSON: ${e.message}")
        emptyList()
    }
}

private fun parseJobFromJson(jobJson: JSONObject): com.hirehi.domain.model.Job? {
    return try {
        val id = jobJson.getString("id")
        val title = jobJson.getString("title")
        
        val company = if (jobJson.has("company") && !jobJson.isNull("company")) {
            jobJson.getString("company")
        } else "Не указано"
        
        val salary = if (jobJson.has("salary") && !jobJson.isNull("salary")) {
            jobJson.getString("salary")
        } else null
        
        val level = if (jobJson.has("level") && !jobJson.isNull("level")) {
            jobJson.getString("level")
        } else "Не указано"
        
        val format = if (jobJson.has("format") && !jobJson.isNull("format")) {
            jobJson.getString("format")
        } else "Не указано"
        
        val url = if (jobJson.has("url") && !jobJson.isNull("url")) {
            jobJson.getString("url")
        } else "https://hirehi.ru"
        
        val description = if (jobJson.has("description") && !jobJson.isNull("description")) {
            jobJson.getString("description")
        } else null
        
        com.hirehi.domain.model.Job(
            id = id,
            title = title,
            company = company,
            salary = salary,
            level = level,
            format = format,
            url = url,
            description = description
        )
    } catch (e: Exception) {
        println("❌ Ошибка при парсинге вакансии: ${e.message}")
        null
    }
}

private fun getPort(): Int {
    return System.getenv("PORT")?.toIntOrNull() ?: 10000
}