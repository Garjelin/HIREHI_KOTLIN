package com.hirehi.data.remote

import com.hirehi.domain.model.Job
import com.hirehi.domain.model.JobSearchParams
import com.hirehi.domain.repository.JobScraper
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.JSONObject
import org.json.JSONArray
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HireHiScraper : JobScraper {

    private val baseUrl = "https://hirehi.ru"
    private val apiUrl = "$baseUrl/api/search/jobs"
    private val httpClient = HttpClient(CIO) {
        expectSuccess = false
    }
    
    private val userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"

    override suspend fun scrapeJobs(params: JobSearchParams): List<Job> {
        val allJobs = getAllJobs()
        return filterJobsByKeywords(allJobs, params.keywords)
    }

    suspend fun getAllJobs(): List<Job> {
        val allJobs = mutableListOf<Job>()
        var page = 1
        val limit = 27

        while (true) {
            val data = getJobsPage(page, limit)
            if (data == null) {
                println("Не удалось получить данные для страницы $page")
                break
            }

            val jobs = data.optJSONArray("jobs")
            if (jobs == null || jobs.length() == 0) {
                println("Страница $page пуста, завершаем сбор данных")
                break
            }

            for (i in 0 until jobs.length()) {
                val jobJson = jobs.getJSONObject(i)
                val job = parseJobFromJson(jobJson)
                if (job != null) {
                    allJobs.add(job)
                }
            }
            
            println("Всего собрано вакансий: ${allJobs.size}")
            
            val hasMore = data.optBoolean("has_more", false)
            if (!hasMore) {
                println("Достигнута последняя страница: $page")
                break
            }
            
            page++
            kotlinx.coroutines.delay(1000) // Небольшая пауза между запросами
        }
        
        return allJobs
    }

    fun filterJobsByKeywords(jobs: List<Job>, keywords: List<String>): List<Job> {
        if (keywords.isEmpty()) return jobs
        
        return jobs.filter { job ->
            val searchText = "${job.title} ${job.company} ${job.description ?: ""} ${job.requirements.joinToString(" ")}".lowercase()
            keywords.any { keyword ->
                searchText.contains(keyword.lowercase())
            }
        }
    }

    private suspend fun getJobsPage(page: Int = 1, limit: Int = 27): JSONObject? {
        println("Запрашиваем страницу $page с лимитом $limit")
        
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

    private fun parseJobFromJson(jobJson: JSONObject): Job? {
        return try {
            val id = jobJson.optString("id", "unknown")
            val title = jobJson.optString("title", "Не указано")
            
            // Компания может быть строкой или объектом
            val company = when {
                jobJson.has("company") && !jobJson.isNull("company") -> {
                    val companyObj = jobJson.get("company")
                    if (companyObj is JSONObject) {
                        companyObj.optString("name", "Не указано")
                    } else {
                        companyObj.toString()
                    }
                }
                else -> "Не указано"
            }
            
            val salary = if (jobJson.has("salary") && !jobJson.isNull("salary")) {
                jobJson.getString("salary")
            } else null
            
            val level = jobJson.optString("level", "Не указано")
            val format = jobJson.optString("format", "Не указано")
            val description = if (jobJson.has("description_details") && !jobJson.isNull("description_details")) {
                jobJson.getString("description_details")
            } else null
            
            val requirements = if (jobJson.has("requirements_details") && !jobJson.isNull("requirements_details")) {
                listOf(jobJson.getString("requirements_details"))
            } else emptyList()
            
            // Используем оригинальную ссылку из API, если она есть
            val url = if (jobJson.has("link") && !jobJson.isNull("link")) {
                jobJson.getString("link")
            } else {
                generateJobUrl(id)
            }
            
            val publishedAt = if (jobJson.has("created_at") && !jobJson.isNull("created_at")) {
                jobJson.getString("created_at")
            } else null
            
            Job(
                id = id,
                title = title,
                company = company,
                salary = salary,
                level = level,
                format = format,
                url = url,
                description = description,
                requirements = requirements,
                publishedAt = publishedAt
            )
        } catch (e: Exception) {
            println("Ошибка при парсинге вакансии: ${e.message}")
            null
        }
    }

    private fun generateJobUrl(jobId: String): String {
        return "$baseUrl/qa/qa-testirovshchik-auto-$jobId"
    }

    fun close() {
        httpClient.close()
    }
}
