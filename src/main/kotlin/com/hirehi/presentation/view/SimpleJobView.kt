package com.hirehi.presentation.view

import com.hirehi.domain.model.Job
import kotlinx.html.*

class SimpleJobView {
    
    fun renderJobsPage(html: HTML, jobs: List<Job>) {
        html.head {
            meta(charset = "UTF-8")
            meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
            title("QA Вакансии - HireHi")
            style {
                +"""
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    .job-card { border: 1px solid #ddd; padding: 20px; margin: 10px 0; border-radius: 8px; }
                    .job-title { color: #333; margin-bottom: 10px; }
                    .job-company { color: #666; font-weight: bold; }
                    .job-salary { color: #e74c3c; font-weight: bold; }
                    .job-link { display: inline-block; background: #3498db; color: white; padding: 8px 16px; text-decoration: none; border-radius: 4px; margin-top: 10px; }
                    .job-link:hover { background: #2980b9; }
                    .stats { background: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
                """
            }
        }
        
        html.body {
            div("stats") {
                h1 { +"QA Вакансии - HireHi" }
                p { +"Senior/Middle QA Automation с Kotlin/Android" }
                p { +"${jobs.size} Всего вакансий" }
                p { +"${jobs.size} После фильтрации" }
                p { +"Обновлено: ${getCurrentTime()}" }
                p { +"Источник: hirehi.ru" }
            }
            
            if (jobs.isEmpty()) {
                div("job-card") {
                    h3 { +"Вакансии не найдены" }
                    p { +"Попробуйте обновить данные или изменить параметры поиска" }
                }
            } else {
                jobs.forEach { job ->
                    div("job-card") {
                        h3("job-title") { +job.title }
                        p("job-company") { +"Компания: ${job.company}" }
                        if (!job.salary.isNullOrEmpty()) {
                            p("job-salary") { +"Зарплата: ${job.salary}" }
                        }
                        p { +"Уровень: ${job.level}" }
                        p { +"Формат: ${job.format}" }
                        a(href = job.url, target = "_blank", classes = "job-link") { +"Посмотреть вакансию" }
                    }
                }
            }
        }
    }
    
    private fun getCurrentTime(): String {
        return java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    }
}
