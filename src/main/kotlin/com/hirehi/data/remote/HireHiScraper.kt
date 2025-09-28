package com.hirehi.data.remote

import com.hirehi.domain.model.Job
import com.hirehi.domain.model.JobSearchParams
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HireHiScraper {
    
    private val baseUrl = "https://hirehi.ru"
    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    
    suspend fun scrapeJobs(params: JobSearchParams): List<Job> {
        val jobs = mutableListOf<Job>()
        
        try {
            val searchUrl = buildSearchUrl(params)
            val document = Jsoup.connect(searchUrl)
                .userAgent(userAgent)
                .timeout(10000)
                .get()
            
            val jobElements = document.select(".job-item, .vacancy-item, .card")
            
            for (element in jobElements) {
                try {
                    val job = parseJobElement(element, baseUrl)
                    if (job != null && matchesKeywords(job, params.keywords)) {
                        jobs.add(job)
                    }
                } catch (e: Exception) {
                    println("Error parsing job element: ${e.message}")
                }
            }
            
            // Если не нашли элементы с ожидаемыми селекторами, попробуем альтернативные
            if (jobs.isEmpty()) {
                val alternativeElements = document.select("a[href*='/qa/'], .job, .vacancy")
                for (element in alternativeElements) {
                    try {
                        val job = parseJobElementAlternative(element, baseUrl)
                        if (job != null && matchesKeywords(job, params.keywords)) {
                            jobs.add(job)
                        }
                    } catch (e: Exception) {
                        println("Error parsing alternative job element: ${e.message}")
                    }
                }
            }
            
        } catch (e: Exception) {
            println("Error scraping jobs: ${e.message}")
        }
        
        return jobs.distinctBy { it.url }
    }
    
    private fun buildSearchUrl(params: JobSearchParams): String {
        val queryParams = mutableListOf<String>()
        
        queryParams.add("category=${URLEncoder.encode(params.category, StandardCharsets.UTF_8)}")
        queryParams.add("format=${URLEncoder.encode(params.format, StandardCharsets.UTF_8)}")
        
        if (params.levels.isNotEmpty()) {
            params.levels.forEach { level ->
                queryParams.add("level=${URLEncoder.encode(level, StandardCharsets.UTF_8)}")
            }
        }
        
        if (params.subcategory.isNotEmpty()) {
            queryParams.add("subcategory=${URLEncoder.encode(params.subcategory, StandardCharsets.UTF_8)}")
        }
        
        // Добавляем ключевые слова в поиск
        if (params.keywords.isNotEmpty()) {
            val searchQuery = params.keywords.joinToString(" OR ")
            queryParams.add("q=${URLEncoder.encode(searchQuery, StandardCharsets.UTF_8)}")
        }
        
        return "$baseUrl/search?${queryParams.joinToString("&")}"
    }
    
    private fun parseJobElement(element: Element, baseUrl: String): Job? {
        return try {
            val titleElement = element.selectFirst("h3, h4, .title, .job-title, .vacancy-title")
            val companyElement = element.selectFirst(".company, .employer, .company-name")
            val salaryElement = element.selectFirst(".salary, .wage, .money")
            val levelElement = element.selectFirst(".level, .experience, .seniority")
            val formatElement = element.selectFirst(".format, .work-type, .remote")
            val linkElement = element.selectFirst("a[href]")
            
            val title = titleElement?.text()?.trim() ?: return null
            val company = companyElement?.text()?.trim() ?: "Не указано"
            val salary = salaryElement?.text()?.trim()
            val level = levelElement?.text()?.trim() ?: "Не указано"
            val format = formatElement?.text()?.trim() ?: "Не указано"
            val url = linkElement?.attr("href")?.let { href ->
                if (href.startsWith("http")) href else "$baseUrl$href"
            } ?: return null
            
            val id = url.substringAfterLast("/").takeIf { it.isNotEmpty() } ?: 
                    title.hashCode().toString()
            
            Job(
                id = id,
                title = title,
                company = company,
                salary = salary,
                level = level,
                format = format,
                url = url,
                publishedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseJobElementAlternative(element: Element, baseUrl: String): Job? {
        return try {
            val title = element.text().trim()
            if (title.isEmpty()) return null
            
            val url = element.attr("href").let { href ->
                if (href.startsWith("http")) href else "$baseUrl$href"
            }
            
            val id = url.substringAfterLast("/").takeIf { it.isNotEmpty() } ?: 
                    title.hashCode().toString()
            
            Job(
                id = id,
                title = title,
                company = "Не указано",
                salary = null,
                level = "Не указано",
                format = "Не указано",
                url = url,
                publishedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun matchesKeywords(job: Job, keywords: List<String>): Boolean {
        if (keywords.isEmpty()) return true
        
        val searchText = "${job.title} ${job.company} ${job.description ?: ""}".lowercase()
        return keywords.any { keyword ->
            searchText.contains(keyword.lowercase())
        }
    }
}
