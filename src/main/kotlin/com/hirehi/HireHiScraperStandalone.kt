package com.hirehi

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.JSONObject
import org.json.JSONArray
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class JobData(
    val id: String,
    val title: String,
    val company: String,
    val salary: String?,
    val level: String,
    val format: String,
    val url: String,
    val description: String? = null,
    val requirements: String? = null
)

class HireHiScraperStandalone {
    private val baseUrl = "https://hirehi.ru"
    private val apiUrl = "$baseUrl/api/search/jobs"
    private val httpClient = HttpClient(CIO) {
        expectSuccess = false
    }
    
    private val userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    
    suspend fun getJobsPage(page: Int = 1, limit: Int = 27): JSONObject? {
        println("Запрашиваем страницу $page с лимитом $limit")
        
        val params = mapOf(
            "page" to page.toString(),
            "limit" to limit.toString(),
            "sort" to "date",
            "category" to "qa",
            "format" to "удалённо",
            "level" to "senior,middle",
            "subcategory" to "auto"
        )
        
        return try {
            val response = httpClient.get(apiUrl) {
                headers {
                    append(HttpHeaders.UserAgent, userAgent)
                    append(HttpHeaders.Accept, "application/json, text/plain, */*")
                    append(HttpHeaders.AcceptLanguage, "ru-RU,ru;q=0.9,en;q=0.8")
                    append(HttpHeaders.Referrer, "$baseUrl/")
                }
                url {
                    parameters.append("page", page.toString())
                    parameters.append("limit", limit.toString())
                    parameters.append("sort", "date")
                    parameters.append("category", "qa")
                    parameters.append("format", "удалённо")
                    parameters.append("level", "senior")
                    parameters.append("level", "middle")
                    parameters.append("subcategory", "auto")
                }
            }
            
            println("API response status: ${response.status}")
            val responseText = response.bodyAsText()
            println("Response content length: ${responseText.length}")
            println("Response content: $responseText")
            
            val jsonObject = JSONObject(responseText)
            val jobsArray = jsonObject.getJSONArray("jobs")
            
            println("Получено ${jobsArray.length()} вакансий на странице $page")
            
            jsonObject
            
        } catch (e: Exception) {
            println("Ошибка при запросе страницы $page: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    suspend fun getAllJobs(): List<JSONObject> {
        val allJobs = mutableListOf<JSONObject>()
        var page = 1
        val limit = 27
        
        while (true) {
            val data = getJobsPage(page, limit)
            
            if (data == null) {
                println("Не удалось получить данные для страницы $page")
                break
            }
            
            val jobsArray = data.getJSONArray("jobs")
            if (jobsArray.length() == 0) {
                println("Страница $page пуста, завершаем сбор данных")
                break
            }
            
            for (i in 0 until jobsArray.length()) {
                allJobs.add(jobsArray.getJSONObject(i))
            }
            
            println("Всего собрано вакансий: ${allJobs.size}")
            
            val hasMore = data.optBoolean("has_more", false)
            if (!hasMore) {
                println("Достигнута последняя страница: $page")
                break
            }
            
            page++
            
            // Небольшая пауза между запросами
            kotlinx.coroutines.delay(1000)
        }
        
        return allJobs
    }
    
    fun extractJobInfo(job: JSONObject): JobData {
        val id = job.optString("id", "unknown")
        val title = job.optString("title", "Не указано")
        
        // Компания может быть строкой или объектом
        val company = when {
            job.has("company") && !job.isNull("company") -> {
                val companyObj = job.get("company")
                if (companyObj is JSONObject) {
                    companyObj.optString("name", "Не указано")
                } else {
                    companyObj.toString()
                }
            }
            else -> "Не указано"
        }
        
        val salary = if (job.has("salary") && !job.isNull("salary")) {
            job.getString("salary")
        } else null
        
        val level = job.optString("level", "Не указано")
        val format = job.optString("format", "Не указано")
        val description = if (job.has("description_details") && !job.isNull("description_details")) {
            job.getString("description_details")
        } else null
        
        val requirements = if (job.has("requirements_details") && !job.isNull("requirements_details")) {
            job.getString("requirements_details")
        } else null
        
        return JobData(
            id = id,
            title = title,
            company = company,
            salary = salary,
            level = level,
            format = format,
            url = generateJobUrl(id),
            description = description,
            requirements = requirements
        )
    }
    
    fun filterJobsByKeywords(jobs: List<JSONObject>, keywords: List<String>): List<JSONObject> {
        val filteredJobs = mutableListOf<JSONObject>()
        
        for (job in jobs) {
            val title = job.optString("title", "").lowercase()
            val description = if (job.has("description_details") && !job.isNull("description_details")) {
                job.getString("description_details").lowercase()
            } else ""
            
            val requirements = if (job.has("requirements_details") && !job.isNull("requirements_details")) {
                job.getString("requirements_details").lowercase()
            } else ""
            
            val combinedText = "$title $description $requirements"
            
            if (keywords.any { keyword -> combinedText.contains(keyword.lowercase()) }) {
                filteredJobs.add(job)
                println("Вакансия '$title' прошла фильтр по ключевым словам")
            } else {
                println("Вакансия '$title' не прошла фильтр по ключевым словам")
            }
        }
        
        return filteredJobs
    }
    
    private fun generateJobUrl(jobId: String): String {
        return "$baseUrl/qa/qa-testirovshchik-auto-$jobId"
    }
    
    fun logJobs(jobs: List<JSONObject>) {
        println("=".repeat(80))
        println("НАЙДЕНО ВАКАНСИЙ: ${jobs.size}")
        println("=".repeat(80))
        
        jobs.forEachIndexed { index, job ->
            val jobInfo = extractJobInfo(job)
            println("${(index + 1).toString().padStart(3)}. ${jobInfo.company} - ${jobInfo.title}")
            println("     Зарплата: ${jobInfo.salary ?: "Не указано"}")
            println("     Уровень: ${jobInfo.level} | Формат: ${jobInfo.format}")
            println("     Ссылка: ${jobInfo.url}")
            println("-".repeat(60))
        }
    }
    
    fun saveToJson(jobs: List<JSONObject>, filename: String = "hirehi_jobs.json") {
        try {
            val jsonArray = JSONArray()
            jobs.forEach { job ->
                jsonArray.put(job)
            }
            
            val result = JSONObject()
            result.put("jobs", jsonArray)
            result.put("total", jobs.size)
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            
            File(filename).writeText(result.toString(2), Charsets.UTF_8)
            println("Данные сохранены в файл: $filename")
        } catch (e: Exception) {
            println("Ошибка при сохранении в файл $filename: ${e.message}")
        }
    }
    
    fun saveFilteredToJson(jobs: List<JSONObject>, filename: String = "hirehi_filtered_jobs.json") {
        try {
            val jobDataList = jobs.map { extractJobInfo(it) }
            val jsonArray = JSONArray()
            
            jobDataList.forEach { jobData ->
                val jobJson = JSONObject()
                jobJson.put("id", jobData.id)
                jobJson.put("title", jobData.title)
                jobJson.put("company", jobData.company)
                jobJson.put("salary", jobData.salary)
                jobJson.put("level", jobData.level)
                jobJson.put("format", jobData.format)
                jobJson.put("url", jobData.url)
                jobJson.put("description", jobData.description)
                jobJson.put("requirements", jobData.requirements)
                jsonArray.put(jobJson)
            }
            
            val result = JSONObject()
            result.put("jobs", jsonArray)
            result.put("total", jobs.size)
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            
            File(filename).writeText(result.toString(2), Charsets.UTF_8)
            println("Отфильтрованные данные сохранены в файл: $filename")
        } catch (e: Exception) {
            println("Ошибка при сохранении отфильтрованных данных в файл $filename: ${e.message}")
        }
    }
}

suspend fun main() {
    println("Запуск скрапера hirehi.ru")
    println("Фильтрация по ключевым словам: Kotlin, Android")
    
    val scraper = HireHiScraperStandalone()
    val keywords = listOf("Kotlin", "Android")
    
    try {
        // Получаем все вакансии
        val allJobs = scraper.getAllJobs()
        
        if (allJobs.isEmpty()) {
            println("Не удалось получить ни одной вакансии")
            return
        }
        
        println("Получено ${allJobs.size} вакансий до фильтрации")
        
        // Сохраняем все вакансии в JSON
        scraper.saveToJson(allJobs, "hirehi_all_jobs.json")
        
        // Фильтруем по ключевым словам
        val filteredJobs = scraper.filterJobsByKeywords(allJobs, keywords)
        
        println("После фильтрации по ключевым словам: ${filteredJobs.size} вакансий")
        
        if (filteredJobs.isEmpty()) {
            println("После фильтрации не осталось ни одной вакансии")
            return
        }
        
        // Выводим информацию в лог
        scraper.logJobs(filteredJobs)
        
        // Сохраняем отфильтрованные данные в JSON
        scraper.saveFilteredToJson(filteredJobs, "hirehi_filtered_jobs.json")
        
        println("Скрапинг завершен успешно!")
        println("Созданы файлы:")
        println("- hirehi_all_jobs.json (все вакансии)")
        println("- hirehi_filtered_jobs.json (отфильтрованные вакансии)")
        
    } catch (e: Exception) {
        println("Критическая ошибка: ${e.message}")
        e.printStackTrace()
    }
}