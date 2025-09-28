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
                val jobs = jobService.loadAndSaveJobs(searchParams)
                if (jobs.isNotEmpty()) {
                    println("✅ Получено ${jobs.size} отфильтрованных вакансий")
                } else {
                    println("⚠️ Не удалось получить вакансии")
                }
            } catch (e: Exception) {
                println("❌ Ошибка при получении данных: ${e.message}")
            }
            
            // Генерируем HTML страницу с встроенными данными
            println("🌐 Генерируем HTML страницу с результатами...")
            val jobs = jobService.loadJobsFromJson()
            val html = jobService.generateHtmlPage(jobs)
            jobService.saveHtmlToFile(html)
            println("✅ HTML страница сгенерирована с ${jobs.size} вакансиями")
            
            // Закрываем ресурсы
            jobService.close()
        }
    }
}
