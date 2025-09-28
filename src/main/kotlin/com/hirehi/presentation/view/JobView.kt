package com.hirehi.presentation.view

import com.hirehi.domain.model.Job
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class JobView {

    fun generateHtmlPage(jobs: List<Job>): String {
        val currentTime = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        
        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang=\"ru\">")
            appendLine("<head>")
            appendLine("    <meta charset=\"UTF-8\">")
            appendLine("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            appendLine("    <title>QA Вакансии - HireHi (${jobs.size} вакансий)</title>")
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
            appendLine("            <p><strong>📊 Всего вакансий:</strong> ${jobs.size}</p>")
            appendLine("            <p><strong>✅ После фильтрации:</strong> ${jobs.size}</p>")
            appendLine("            <p><strong>🕒 Обновлено:</strong> $currentTime</p>")
            appendLine("            <p><strong>🌐 Источник:</strong> hirehi.ru</p>")
            appendLine("        </div>")
            
            if (jobs.isEmpty()) {
                appendLine("        <div class=\"job-card\">")
                appendLine("            <h3>Вакансии не найдены</h3>")
                appendLine("            <p>Попробуйте обновить данные или изменить параметры поиска</p>")
                appendLine("        </div>")
            } else {
                jobs.forEach { job ->
                    appendLine("        <div class=\"job-card\">")
                    appendLine("            <h3 class=\"job-title\">${job.title}</h3>")
                    appendLine("            <p class=\"job-company\">Компания: ${job.company}</p>")
                    if (!job.salary.isNullOrEmpty()) {
                        appendLine("            <p class=\"job-salary\">Зарплата: ${job.salary}</p>")
                    }
                    appendLine("            <p class=\"job-level\">Уровень: ${job.level}</p>")
                    appendLine("            <p class=\"job-format\">Формат: ${job.format}</p>")
                    appendLine("            <a href=\"${job.url}\" target=\"_blank\" class=\"job-link\">Посмотреть вакансию</a>")
                    appendLine("        </div>")
                }
            }
            
            appendLine("        <div style=\"text-align: center; margin-top: 30px; padding: 20px; background: #d4edda; border-radius: 8px;\">")
            appendLine("            <p><strong>🎉 Успешно найдено ${jobs.size} вакансий с Kotlin/Android!</strong></p>")
            appendLine("            <p>Все вакансии соответствуют критериям: Senior/Middle QA Automation с удаленной работой</p>")
            appendLine("        </div>")
            appendLine("    </div>")
            appendLine("</body>")
            appendLine("</html>")
        }
    }
}
