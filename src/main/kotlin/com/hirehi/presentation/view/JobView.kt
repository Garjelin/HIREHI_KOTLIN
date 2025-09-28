package com.hirehi.presentation.view

import com.hirehi.domain.model.Job
import com.hirehi.domain.model.JobStatistics
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class JobView {

    fun generateHtmlPage(jobs: List<Job>, statistics: JobStatistics? = null): String {
        val currentTime = statistics?.lastUpdated?.let { 
            LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        } ?: LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        
        val totalJobs = statistics?.totalJobs ?: jobs.size
        val filteredJobs = statistics?.filteredJobs ?: jobs.size
        val keywords = statistics?.keywords ?: listOf("Kotlin", "Android")
        
        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang=\"ru\">")
            appendLine("<head>")
            appendLine("    <meta charset=\"UTF-8\">")
            appendLine("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            appendLine("    <title>QA Вакансии - HireHi ($filteredJobs из $totalJobs)</title>")
            appendLine("    <style>")
            appendLine("        * { margin: 0; padding: 0; box-sizing: border-box; }")
            appendLine("        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; }")
            appendLine("        .container { max-width: 1200px; margin: 0 auto; padding: 20px; }")
            appendLine("        .header { background: rgba(255,255,255,0.95); backdrop-filter: blur(10px); border-radius: 20px; padding: 30px; margin-bottom: 30px; box-shadow: 0 8px 32px rgba(0,0,0,0.1); }")
            appendLine("        .header h1 { color: #2d3748; font-size: 2.5em; margin-bottom: 20px; text-align: center; }")
            appendLine("        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 20px; }")
            appendLine("        .stat-card { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); color: white; padding: 20px; border-radius: 15px; text-align: center; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }")
            appendLine("        .stat-number { font-size: 2em; font-weight: bold; margin-bottom: 5px; }")
            appendLine("        .stat-label { font-size: 0.9em; opacity: 0.9; }")
            appendLine("        .keywords { display: flex; flex-wrap: wrap; gap: 10px; margin: 20px 0; }")
            appendLine("        .keyword-chip { background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%); color: #8b4513; padding: 8px 16px; border-radius: 20px; font-weight: 500; font-size: 0.9em; }")
            appendLine("        .refresh-btn { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border: none; padding: 12px 24px; border-radius: 25px; font-size: 1em; cursor: pointer; transition: transform 0.2s; margin: 20px 0; }")
            appendLine("        .refresh-btn:hover { transform: translateY(-2px); box-shadow: 0 4px 15px rgba(0,0,0,0.2); }")
            appendLine("        .jobs-container { display: grid; gap: 20px; }")
            appendLine("        .job-card { background: rgba(255,255,255,0.95); backdrop-filter: blur(10px); border-radius: 20px; padding: 25px; box-shadow: 0 8px 32px rgba(0,0,0,0.1); transition: transform 0.3s, box-shadow 0.3s; }")
            appendLine("        .job-card:hover { transform: translateY(-5px); box-shadow: 0 12px 40px rgba(0,0,0,0.15); }")
            appendLine("        .job-title { color: #2d3748; font-size: 1.4em; font-weight: 600; margin-bottom: 15px; line-height: 1.3; }")
            appendLine("        .job-company { color: #4a5568; font-size: 1.1em; font-weight: 500; margin-bottom: 15px; }")
            appendLine("        .job-chips { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 20px; }")
            appendLine("        .chip { padding: 6px 12px; border-radius: 15px; font-size: 0.85em; font-weight: 500; display: flex; align-items: center; gap: 5px; }")
            appendLine("        .chip-salary { background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%); color: #8b0000; }")
            appendLine("        .chip-level { background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); color: #006400; }")
            appendLine("        .chip-format { background: linear-gradient(135deg, #d299c2 0%, #fef9d7 100%); color: #4b0082; }")
            appendLine("        .job-link { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; text-decoration: none; padding: 12px 24px; border-radius: 25px; font-weight: 500; display: inline-block; transition: transform 0.2s; }")
            appendLine("        .job-link:hover { transform: translateY(-2px); box-shadow: 0 4px 15px rgba(0,0,0,0.2); }")
            appendLine("        .no-jobs { text-align: center; color: white; font-size: 1.2em; padding: 40px; }")
            appendLine("        .emoji { font-size: 1.2em; }")
            appendLine("    </style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("    <div class=\"container\">")
            appendLine("        <div class=\"header\">")
            appendLine("            <h1>🎯 QA Вакансии - HireHi</h1>")
            appendLine("            <div class=\"stats-grid\">")
            appendLine("                <div class=\"stat-card\">")
            appendLine("                    <div class=\"stat-number\">$totalJobs</div>")
            appendLine("                    <div class=\"stat-label\">Всего вакансий</div>")
            appendLine("                </div>")
            appendLine("                <div class=\"stat-card\">")
            appendLine("                    <div class=\"stat-number\">$filteredJobs</div>")
            appendLine("                    <div class=\"stat-label\">После фильтрации</div>")
            appendLine("                </div>")
            appendLine("                <div class=\"stat-card\">")
            appendLine("                    <div class=\"stat-number\">${(filteredJobs * 100 / totalJobs)}%</div>")
            appendLine("                    <div class=\"stat-label\">Процент совпадений</div>")
            appendLine("                </div>")
            appendLine("            </div>")
            appendLine("            <div class=\"keywords\">")
            appendLine("                <span class=\"keyword-chip\">🔍 Фильтр:</span>")
            keywords.forEach { keyword ->
                appendLine("                <span class=\"keyword-chip\">$keyword</span>")
            }
            appendLine("            </div>")
            appendLine("            <div style=\"text-align: center;\">")
            appendLine("                <button class=\"refresh-btn\" onclick=\"location.reload()\">🔄 Обновить данные</button>")
            appendLine("            </div>")
            appendLine("            <div style=\"text-align: center; margin-top: 15px; color: #666; font-size: 0.9em;\">")
            appendLine("                🕒 Обновлено: $currentTime | 🌐 Источник: hirehi.ru")
            appendLine("            </div>")
            appendLine("        </div>")
            
            if (jobs.isEmpty()) {
                appendLine("        <div class=\"no-jobs\">")
                appendLine("            <div class=\"emoji\">😔</div>")
                appendLine("            <div>Вакансии не найдены</div>")
                appendLine("            <div style=\"font-size: 0.9em; margin-top: 10px; opacity: 0.8;\">Попробуйте обновить данные или изменить параметры поиска</div>")
                appendLine("        </div>")
            } else {
                appendLine("        <div class=\"jobs-container\">")
                jobs.forEach { job ->
                    appendLine("            <div class=\"job-card\">")
                    appendLine("                <div class=\"job-title\">${job.title}</div>")
                    appendLine("                <div class=\"job-company\">🏢 ${job.company}</div>")
                    appendLine("                <div class=\"job-chips\">")
                    if (!job.salary.isNullOrEmpty()) {
                        appendLine("                    <div class=\"chip chip-salary\"><span class=\"emoji\">💰</span> ${job.salary}</div>")
                    }
                    appendLine("                    <div class=\"chip chip-level\"><span class=\"emoji\">📊</span> ${job.level}</div>")
                    appendLine("                    <div class=\"chip chip-format\"><span class=\"emoji\">🏠</span> ${job.format}</div>")
                    appendLine("                </div>")
                    appendLine("                <a href=\"${job.url}\" target=\"_blank\" class=\"job-link\">👀 Посмотреть вакансию</a>")
                    appendLine("            </div>")
                }
                appendLine("        </div>")
            }
            
            appendLine("    </div>")
            appendLine("</body>")
            appendLine("</html>")
        }
    }
}