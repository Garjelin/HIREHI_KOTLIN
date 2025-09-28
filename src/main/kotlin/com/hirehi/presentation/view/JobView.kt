package com.hirehi.presentation.view

import com.hirehi.domain.model.Job
import kotlinx.html.*
import kotlinx.html.stream.createHTML

class JobView {
    
    fun renderJobsPage(html: HTML, jobs: List<Job>) {
        html.head {
            meta(charset = "UTF-8")
            meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
            title("QA Вакансии - HireHi")
            style {
                +"""
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        min-height: 100vh;
                        padding: 20px;
                    }
                    
                    .container {
                        max-width: 1200px;
                        margin: 0 auto;
                        background: white;
                        border-radius: 20px;
                        box-shadow: 0 20px 40px rgba(0,0,0,0.1);
                        overflow: hidden;
                    }
                    
                    .header {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 40px;
                        text-align: center;
                    }
                    
                    .header h1 {
                        font-size: 2.5rem;
                        margin-bottom: 10px;
                        font-weight: 700;
                    }
                    
                    .header p {
                        font-size: 1.1rem;
                        opacity: 0.9;
                    }
                    
                    .controls {
                        padding: 30px 40px;
                        background: #f8f9fa;
                        border-bottom: 1px solid #e9ecef;
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        flex-wrap: wrap;
                        gap: 20px;
                    }
                    
                    .stats {
                        display: flex;
                        gap: 20px;
                        align-items: center;
                    }
                    
                    .stat-item {
                        background: white;
                        padding: 10px 20px;
                        border-radius: 25px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        font-weight: 600;
                        color: #495057;
                    }
                    
                    .refresh-btn {
                        background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
                        color: white;
                        border: none;
                        padding: 12px 24px;
                        border-radius: 25px;
                        font-size: 1rem;
                        font-weight: 600;
                        cursor: pointer;
                        transition: all 0.3s ease;
                        box-shadow: 0 4px 15px rgba(40, 167, 69, 0.3);
                    }
                    
                    .refresh-btn:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 6px 20px rgba(40, 167, 69, 0.4);
                    }
                    
                    .jobs-container {
                        padding: 40px;
                    }
                    
                    .jobs-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
                        gap: 25px;
                    }
                    
                    .job-card {
                        background: white;
                        border: 1px solid #e9ecef;
                        border-radius: 15px;
                        padding: 25px;
                        transition: all 0.3s ease;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.05);
                    }
                    
                    .job-card:hover {
                        transform: translateY(-5px);
                        box-shadow: 0 10px 30px rgba(0,0,0,0.15);
                        border-color: #667eea;
                    }
                    
                    .job-title {
                        font-size: 1.3rem;
                        font-weight: 700;
                        color: #2c3e50;
                        margin-bottom: 10px;
                        line-height: 1.3;
                    }
                    
                    .job-company {
                        font-size: 1.1rem;
                        color: #667eea;
                        font-weight: 600;
                        margin-bottom: 15px;
                    }
                    
                    .job-details {
                        display: flex;
                        flex-direction: column;
                        gap: 8px;
                        margin-bottom: 20px;
                    }
                    
                    .job-detail {
                        display: flex;
                        align-items: center;
                        gap: 8px;
                        font-size: 0.95rem;
                        color: #6c757d;
                    }
                    
                    .job-detail strong {
                        color: #495057;
                        font-weight: 600;
                    }
                    
                    .job-salary {
                        background: linear-gradient(135deg, #ffc107 0%, #fd7e14 100%);
                        color: white;
                        padding: 8px 16px;
                        border-radius: 20px;
                        font-weight: 700;
                        font-size: 1.1rem;
                        text-align: center;
                        margin-bottom: 15px;
                    }
                    
                    .job-link {
                        display: inline-block;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        text-decoration: none;
                        padding: 12px 24px;
                        border-radius: 25px;
                        font-weight: 600;
                        text-align: center;
                        transition: all 0.3s ease;
                        width: 100%;
                    }
                    
                    .job-link:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
                        color: white;
                        text-decoration: none;
                    }
                    
                    .no-jobs {
                        text-align: center;
                        padding: 60px 20px;
                        color: #6c757d;
                    }
                    
                    .no-jobs h3 {
                        font-size: 1.5rem;
                        margin-bottom: 10px;
                        color: #495057;
                    }
                    
                    .loading {
                        text-align: center;
                        padding: 40px;
                        color: #6c757d;
                    }
                    
                    .spinner {
                        border: 4px solid #f3f3f3;
                        border-top: 4px solid #667eea;
                        border-radius: 50%;
                        width: 40px;
                        height: 40px;
                        animation: spin 1s linear infinite;
                        margin: 0 auto 20px;
                    }
                    
                    @keyframes spin {
                        0% { transform: rotate(0deg); }
                        100% { transform: rotate(360deg); }
                    }
                    
                    @media (max-width: 768px) {
                        .header h1 {
                            font-size: 2rem;
                        }
                        
                        .controls {
                            flex-direction: column;
                            text-align: center;
                        }
                        
                        .stats {
                            justify-content: center;
                        }
                        
                        .jobs-grid {
                            grid-template-columns: 1fr;
                        }
                    }
                """
            }
        }
        
        html.body {
            div("container") {
                div("header") {
                    h1 { +"QA Вакансии - HireHi" }
                    p { +"Автоматический сбор вакансий с фильтрацией по Kotlin и Android" }
                }
                
                div("controls") {
                    div("stats") {
                        div("stat-item") {
                            +"Найдено вакансий: ${jobs.size}"
                        }
                        div("stat-item") {
                            +"Обновлено: ${getCurrentTime()}"
                        }
                    }
                    
                    button(classes = "refresh-btn") {
                        id = "refreshBtn"
                        +"🔄 Обновить данные"
                        onClick = "refreshJobs()"
                    }
                }
                
                div("jobs-container") {
                    if (jobs.isEmpty()) {
                        div("no-jobs") {
                            h3 { +"Вакансии не найдены" }
                            p { +"Попробуйте обновить данные или изменить параметры поиска" }
                        }
                    } else {
                        div("jobs-grid") {
                            jobs.forEach { job ->
                                div("job-card") {
                                    h3("job-title") { +job.title }
                                    div("job-company") { +job.company }
                                    
                                    if (!job.salary.isNullOrEmpty()) {
                                        div("job-salary") { +job.salary }
                                    }
                                    
                                    div("job-details") {
                                        div("job-detail") {
                                            strong { +"Уровень:" }
                                            span { +job.level }
                                        }
                                        div("job-detail") {
                                            strong { +"Формат:" }
                                            span { +job.format }
                                        }
                                        if (!job.publishedAt.isNullOrEmpty()) {
                                            div("job-detail") {
                                                strong { +"Опубликовано:" }
                                                span { +formatDate(job.publishedAt) }
                                            }
                                        }
                                    }
                                    
                                    a(href = job.url, classes = "job-link", target = "_blank") {
                                        +"Посмотреть вакансию"
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            script {
                +"""
                    async function refreshJobs() {
                        const btn = document.getElementById('refreshBtn');
                        const originalText = btn.textContent;
                        
                        btn.textContent = '🔄 Обновление...';
                        btn.disabled = true;
                        
                        try {
                            const response = await fetch('/api/refresh', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/json'
                                }
                            });
                            
                            if (response.ok) {
                                location.reload();
                            } else {
                                alert('Ошибка при обновлении данных');
                            }
                        } catch (error) {
                            console.error('Error:', error);
                            alert('Ошибка при обновлении данных');
                        } finally {
                            btn.textContent = originalText;
                            btn.disabled = false;
                        }
                    }
                """
            }
        }
    }
    
    private fun getCurrentTime(): String {
        return java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    }
    
    private fun formatDate(dateString: String): String {
        return try {
            val date = java.time.LocalDateTime.parse(dateString, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        } catch (e: Exception) {
            dateString
        }
    }
}
