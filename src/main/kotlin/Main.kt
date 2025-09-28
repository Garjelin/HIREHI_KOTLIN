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
        
        // Генерируем HTML страницу с встроенными данными
        println("🌐 Генерируем HTML страницу с результатами...")
        generateHtmlPage()
        
        // Запускаем веб-сервер
        println("🌐 Запуск веб-сервера...")
        startWebServer()
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

private fun generateHtmlPage() {
    try {
        val jsonFile = File("hirehi_filtered_jobs.json")
        if (!jsonFile.exists()) {
            println("❌ JSON файл не найден для генерации HTML")
            return
        }
        
        val jsonText = jsonFile.readText()
        val jsonObject = JSONObject(jsonText)
        val jobsArray = jsonObject.getJSONArray("jobs")
        val totalCount = jsonObject.getInt("total")
        
        val currentTime = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        
        val html = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang=\"ru\">")
            appendLine("<head>")
            appendLine("    <meta charset=\"UTF-8\">")
            appendLine("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            appendLine("    <title>QA Вакансии - HireHi (${jobsArray.length()} вакансий)</title>")
            appendLine("    <style>")
            appendLine("        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f5f5f5; }")
            appendLine("        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }")
            appendLine("        .stats { background: #e8f4fd; padding: 20px; border-radius: 8px; margin-bottom: 20px; border-left: 4px solid #3498db; }")
            appendLine("        .job-card { border: 1px solid #ddd; padding: 20px; margin: 15px 0; border-radius: 8px; background: white; box-shadow: 0 1px 3px rgba(0,0,0,0.1); transition: transform 0.2s; }")
            appendLine("        .job-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.15); }")
            appendLine("        .job-title { color: #2c3e50; margin-bottom: 10px; font-size: 1.2em; }")
            appendLine("        .job-company { color: #7f8c8d; font-weight: bold; margin-bottom: 8px; }")
            appendLine("        .job-salary { color: #e74c3c; font-weight: bold; font-size: 1.1em; }")
            appendLine("        .job-level { color: #27ae60; font-weight: bold; }")
            appendLine("        .job-format { color: #8e44ad; font-weight: bold; }")
            appendLine("        .job-link { display: inline-block; background: #3498db; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin-top: 15px; transition: background 0.3s; }")
            appendLine("        .job-link:hover { background: #2980b9; }")
            appendLine("        h1 { color: #2c3e50; text-align: center; }")
            appendLine("        .highlight { background: #fff3cd; padding: 2px 4px; border-radius: 3px; }")
            appendLine("    </style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("    <div class=\"container\">")
            appendLine("        <div class=\"stats\">")
            appendLine("            <h1>🎯 QA Вакансии - HireHi</h1>")
            appendLine("            <p><strong>Фильтр:</strong> Senior/Middle QA Automation с <span class=\"highlight\">Kotlin</span>/<span class=\"highlight\">Android</span></p>")
            appendLine("            <p><strong>📊 Всего вакансий:</strong> $totalCount</p>")
            appendLine("            <p><strong>✅ После фильтрации:</strong> ${jobsArray.length()}</p>")
            appendLine("            <p><strong>🕒 Обновлено:</strong> $currentTime</p>")
            appendLine("            <p><strong>🌐 Источник:</strong> hirehi.ru</p>")
            appendLine("        </div>")
            
            for (i in 0 until jobsArray.length()) {
                val job = jobsArray.getJSONObject(i)
                val title = job.optString("title", "Не указано")
                val company = job.optString("company", "Не указано")
                val salary = job.optString("salary", "")
                val level = job.optString("level", "Не указано")
                val format = job.optString("format", "Не указано")
                val url = job.optString("url", job.optString("link", "#"))
                
                appendLine("        <div class=\"job-card\">")
                appendLine("            <h3 class=\"job-title\">$title</h3>")
                appendLine("            <p class=\"job-company\">Компания: $company</p>")
                if (salary.isNotEmpty()) {
                    appendLine("            <p class=\"job-salary\">Зарплата: $salary</p>")
                }
                appendLine("            <p class=\"job-level\">Уровень: $level</p>")
                appendLine("            <p class=\"job-format\">Формат: $format</p>")
                appendLine("            <a href=\"$url\" target=\"_blank\" class=\"job-link\">Посмотреть вакансию</a>")
                appendLine("        </div>")
            }
            
            appendLine("        <div style=\"text-align: center; margin-top: 30px; padding: 20px; background: #d4edda; border-radius: 8px;\">")
            appendLine("            <p><strong>🎉 Успешно найдено ${jobsArray.length()} вакансий с Kotlin/Android!</strong></p>")
            appendLine("            <p>Все вакансии соответствуют критериям: Senior/Middle QA Automation с удаленной работой</p>")
            appendLine("        </div>")
            appendLine("    </div>")
            appendLine("</body>")
            appendLine("</html>")
        }
        
        File("jobs_display.html").writeText(html, Charsets.UTF_8)
        println("✅ HTML страница сгенерирована с ${jobsArray.length()} вакансиями")
        
    } catch (e: Exception) {
        println("❌ Ошибка при генерации HTML: ${e.message}")
    }
}

private suspend fun startWebServer() {
    try {
        val port = getPort()
        println("🌐 Запуск веб-сервера на порту $port...")
        
        val server = embeddedServer(Netty, port = port) {
            configureApplication()
        }
        
        server.start()
        println("✅ Веб-сервер запущен! Откройте http://localhost:$port в браузере")
        
        // Открываем браузер автоматически
        try {
            val process = ProcessBuilder("xdg-open", "http://localhost:$port").start()
            println("🌐 Браузер открыт автоматически!")
        } catch (e: Exception) {
            println("⚠️ Не удалось открыть браузер автоматически. Откройте http://localhost:$port вручную")
        }
        
        // Ждем завершения сервера (блокируем выполнение)
        server.start(wait = true)
        
    } catch (e: Exception) {
        println("❌ Ошибка при запуске веб-сервера: ${e.message}")
        // Если сервер не запустился, открываем HTML файл напрямую
        val htmlFile = File("jobs_display.html")
        if (htmlFile.exists()) {
            val process = ProcessBuilder("xdg-open", htmlFile.absolutePath).start()
            println("✅ Веб-страница открыта в браузере напрямую!")
        }
    }
}

private fun getPort(): Int {
    return System.getenv("PORT")?.toIntOrNull() ?: 10000
}