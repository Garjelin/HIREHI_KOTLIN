package com.hirehi.presentation.service

import com.hirehi.data.repository.ArchiveRepositoryImpl
import com.hirehi.data.remote.HireHiScraper
import com.hirehi.data.repository.JobRepositoryImpl
import com.hirehi.domain.model.ArchivedJob
import com.hirehi.domain.model.Job
import com.hirehi.domain.model.JobSearchParams
import com.hirehi.domain.model.JobStatistics
import com.hirehi.domain.usecase.GetArchivedJobsUseCase
import com.hirehi.domain.usecase.GetJobsUseCase
import com.hirehi.presentation.view.ArchiveView
import com.hirehi.presentation.view.JobView
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class JobService {
    
    private val scraper = HireHiScraper()
    private val repository = JobRepositoryImpl(scraper)
    private val getJobsUseCase = GetJobsUseCase(repository)
    private val jobView = JobView()
    private val archiveView = ArchiveView()
    private val archiveRepository = ArchiveRepositoryImpl()
    private val getArchivedJobsUseCase = GetArchivedJobsUseCase(archiveRepository)

    suspend fun loadAndSaveJobs(searchParams: JobSearchParams): JobStatistics {
        // Получаем все вакансии
        val allJobs = scraper.getAllJobs()
        val totalJobs = allJobs.size
        
        // Фильтруем по ключевым словам
        val filteredJobs = scraper.filterJobsByKeywords(allJobs, searchParams.keywords)
        
        // Создаем статистику
        val statistics = JobStatistics(
            totalJobs = totalJobs,
            filteredJobs = filteredJobs.size,
            lastUpdated = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            keywords = searchParams.keywords
        )
        
        // Сохраняем отфильтрованные вакансии и статистику
        saveJobsToJson(filteredJobs, statistics, "hirehi_filtered_jobs.json")
        return statistics
    }

    suspend fun generateHtmlPage(jobs: List<Job>, statistics: JobStatistics? = null): String {
        val archiveCount = getArchiveCount()
        return jobView.generateHtmlPage(jobs, statistics, archiveCount)
    }
    
    fun generateArchivePage(archivedJobs: List<ArchivedJob>): String {
        return archiveView.generateArchivePage(archivedJobs)
    }

    fun saveHtmlToFile(html: String, filename: String = "jobs_display.html") {
        File(filename).writeText(html, Charsets.UTF_8)
    }

    fun loadJobsFromJson(): Pair<List<Job>, JobStatistics?> {
        return try {
            val jsonFile = File("hirehi_filtered_jobs.json")
            if (!jsonFile.exists()) {
                println("⚠️ JSON файл не найден, возвращаем пустой список")
                return Pair(emptyList(), null)
            }

            val jsonText = jsonFile.readText()
            val jsonObject = org.json.JSONObject(jsonText)
            val jsonArray = jsonObject.getJSONArray("jobs")
            val jobs = mutableListOf<Job>()

            for (i in 0 until jsonArray.length()) {
                val jobJson = jsonArray.getJSONObject(i)
                val job = parseJobFromJson(jobJson)
                if (job != null) {
                    jobs.add(job)
                }
            }

            // Загружаем статистику
            val statistics = if (jsonObject.has("statistics")) {
                val statsJson = jsonObject.getJSONObject("statistics")
                JobStatistics(
                    totalJobs = statsJson.getInt("totalJobs"),
                    filteredJobs = statsJson.getInt("filteredJobs"),
                    lastUpdated = statsJson.getString("lastUpdated"),
                    keywords = statsJson.getJSONArray("keywords").let { array ->
                        (0 until array.length()).map { array.getString(it) }
                    }
                )
            } else null

            println("📊 Загружено ${jobs.size} вакансий из JSON файла")
            Pair(jobs, statistics)
        } catch (e: Exception) {
            println("❌ Ошибка при загрузке JSON: ${e.message}")
            Pair(emptyList(), null)
        }
    }

    suspend fun loadJobsFromJsonExcludingArchived(): Pair<List<Job>, JobStatistics?> {
        val (allJobs, statistics) = loadJobsFromJson()
        
        return try {
            // Получаем список архивированных ID
            val archivedJobs = getArchivedJobsUseCase.execute(10000, 0) // Получаем все архивированные
            val archivedIds = archivedJobs.map { it.id }.toSet()
            
            // Фильтруем вакансии, исключая архивированные
            val filteredJobs = allJobs.filter { job -> job.id !in archivedIds }
            
            // Обновляем статистику с учетом архивированных
            val updatedStatistics = statistics?.copy(
                filteredJobs = filteredJobs.size
            )
            
            println("📊 Отфильтровано ${allJobs.size - filteredJobs.size} архивированных вакансий")
            Pair(filteredJobs, updatedStatistics)
        } catch (e: Exception) {
            println("⚠️ Ошибка при фильтрации архивированных вакансий: ${e.message}")
            // В случае ошибки возвращаем все вакансии
            Pair(allJobs, statistics)
        }
    }

    suspend fun getArchiveCount(): Int {
        return try {
            val archivedJobs = getArchivedJobsUseCase.execute(10000, 0)
            archivedJobs.size
        } catch (e: Exception) {
            println("⚠️ Ошибка при получении количества архивированных вакансий: ${e.message}")
            0
        }
    }

    private fun parseJobFromJson(jobJson: org.json.JSONObject): Job? {
        return try {
            val id = jobJson.get("id").toString()
            val title = jobJson.optString("title", "Не указано")

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
            } else if (jobJson.has("link") && !jobJson.isNull("link")) {
                jobJson.getString("link")
            } else "https://hirehi.ru"

            val description = if (jobJson.has("description") && !jobJson.isNull("description")) {
                jobJson.getString("description")
            } else null

            Job(
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

    private fun saveJobsToJson(jobs: List<Job>, statistics: JobStatistics, filename: String) {
        try {
            val jsonArray = org.json.JSONArray()
            
            jobs.forEach { job ->
                val jobJson = org.json.JSONObject()
                jobJson.put("id", job.id)
                jobJson.put("title", job.title)
                jobJson.put("company", job.company)
                jobJson.put("salary", job.salary)
                jobJson.put("level", job.level)
                jobJson.put("format", job.format)
                jobJson.put("url", job.url)
                jobJson.put("description", job.description)
                jobJson.put("requirements", job.requirements.joinToString(" "))
                jsonArray.put(jobJson)
            }
            
            val result = org.json.JSONObject()
            result.put("jobs", jsonArray)
            result.put("total", jobs.size)
            result.put("timestamp", statistics.lastUpdated)
            
            // Добавляем статистику
            val statsJson = org.json.JSONObject()
            statsJson.put("totalJobs", statistics.totalJobs)
            statsJson.put("filteredJobs", statistics.filteredJobs)
            statsJson.put("lastUpdated", statistics.lastUpdated)
            val keywordsArray = org.json.JSONArray()
            statistics.keywords.forEach { keywordsArray.put(it) }
            statsJson.put("keywords", keywordsArray)
            result.put("statistics", statsJson)
            
            File(filename).writeText(result.toString(2), Charsets.UTF_8)
            println("Данные сохранены в файл: $filename")
        } catch (e: Exception) {
            println("Ошибка при сохранении в файл $filename: ${e.message}")
        }
    }

    fun close() {
        scraper.close()
    }
}
