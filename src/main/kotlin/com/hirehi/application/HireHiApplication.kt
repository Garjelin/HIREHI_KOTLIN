package com.hirehi.application

import com.hirehi.domain.model.JobSearchParams
import com.hirehi.presentation.service.JobService
import kotlinx.coroutines.runBlocking

class HireHiApplication {
    
    private val jobService = JobService()

    fun run() {
        runBlocking {
            println("🚀 Запуск HireHi веб-приложения...")

            // Получаем данные используя Clean Architecture
            println("📡 Получение данных с hirehi.ru...")
            
            val searchParams = JobSearchParams(
                keywords = listOf("Kotlin", "Android")
            )

        try {
            val statistics = jobService.loadAndSaveJobs(searchParams)
            println("✅ Получено ${statistics.filteredJobs} отфильтрованных вакансий из ${statistics.totalJobs} общих")
        } catch (e: Exception) {
            println("❌ Ошибка при получении данных: ${e.message}")
        }
        
        // Генерируем HTML страницу с встроенными данными
        println("🌐 Генерируем HTML страницу с результатами...")
        val (jobs, statistics) = jobService.loadJobsFromJson()
        val html = jobService.generateHtmlPage(jobs, statistics)
        jobService.saveHtmlToFile(html)
        println("✅ HTML страница сгенерирована с ${jobs.size} вакансиями")
            
            // Закрываем ресурсы
            jobService.close()
        }
    }
}
