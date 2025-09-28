package com.hirehi.presentation.service

import com.hirehi.data.remote.HireHiScraper
import com.hirehi.data.repository.JobRepositoryImpl
import com.hirehi.domain.model.Job
import com.hirehi.domain.model.JobSearchParams
import com.hirehi.domain.usecase.GetJobsUseCase
import com.hirehi.presentation.view.JobView
import java.io.File

class JobService {
    
    private val scraper = HireHiScraper()
    private val repository = JobRepositoryImpl(scraper)
    private val getJobsUseCase = GetJobsUseCase(repository)
    private val jobView = JobView()

    suspend fun loadAndSaveJobs(searchParams: JobSearchParams): List<Job> {
        val jobs = getJobsUseCase(searchParams)
        saveJobsToJson(jobs, "hirehi_filtered_jobs.json")
        return jobs
    }

    fun generateHtmlPage(jobs: List<Job>): String {
        return jobView.generateHtmlPage(jobs)
    }

    fun saveHtmlToFile(html: String, filename: String = "jobs_display.html") {
        File(filename).writeText(html, Charsets.UTF_8)
    }

    fun loadJobsFromJson(): List<Job> {
        return try {
            val jsonFile = File("hirehi_filtered_jobs.json")
            if (!jsonFile.exists()) {
                println("⚠️ JSON файл не найден, возвращаем пустой список")
                return emptyList()
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

            println("📊 Загружено ${jobs.size} вакансий из JSON файла")
            jobs
        } catch (e: Exception) {
            println("❌ Ошибка при загрузке JSON: ${e.message}")
            emptyList()
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

    private fun saveJobsToJson(jobs: List<Job>, filename: String) {
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
            result.put("timestamp", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            
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
