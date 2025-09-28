package com.hirehi.data.remote

import com.hirehi.domain.model.Job
import com.hirehi.domain.model.JobSearchParams
import com.hirehi.domain.repository.JobScraper

class MockScraper : JobScraper {
    
    override suspend fun scrapeJobs(params: JobSearchParams): List<Job> {
        // Возвращаем тестовые данные вместо реального парсинга
        return listOf(
            Job(
                id = "1",
                title = "QA Engineer (Kotlin/Android)",
                company = "ТехКомпания",
                salary = "от 150 000 ₽",
                level = "middle",
                format = "удалённо",
                url = "https://example.com/job1",
                description = "Ищем QA инженера для работы с мобильными приложениями на Kotlin и Android",
                publishedAt = "2024-01-15T10:30:00"
            ),
            Job(
                id = "2", 
                title = "Senior QA Automation (Kotlin)",
                company = "Стартап",
                salary = "от 200 000 ₽",
                level = "senior",
                format = "удалённо",
                url = "https://example.com/job2",
                description = "Опытный QA для автоматизации тестирования на Kotlin",
                publishedAt = "2024-01-14T15:45:00"
            ),
            Job(
                id = "3",
                title = "QA Mobile (Android/Kotlin)",
                company = "Мобильная компания",
                salary = "от 180 000 ₽",
                level = "middle",
                format = "гибрид",
                url = "https://example.com/job3",
                description = "Тестирование мобильных приложений на Android с использованием Kotlin",
                publishedAt = "2024-01-13T09:20:00"
            )
        )
    }
}
