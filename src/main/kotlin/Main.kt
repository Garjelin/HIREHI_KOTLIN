package com.hirehi

import com.hirehi.data.local.JobLocalDataSource
import com.hirehi.data.remote.HireHiScraper
import com.hirehi.data.repository.JobRepositoryImpl
import com.hirehi.domain.model.JobSearchParams
import com.hirehi.domain.usecase.GetJobsUseCase
import com.hirehi.domain.usecase.RefreshJobsUseCase
import com.hirehi.presentation.view.SimpleJobView
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = getPort()) {
        configureApplication()
    }.start(wait = true)
}

fun Application.configureApplication() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
    
    install(CORS) {
        anyHost()
        allowHeader("Content-Type")
        allowMethod(io.ktor.http.HttpMethod.Get)
        allowMethod(io.ktor.http.HttpMethod.Post)
    }
    
    // Инициализация зависимостей
    val scraper = HireHiScraper()
    val localDataSource = JobLocalDataSource()
    val repository = JobRepositoryImpl(scraper, localDataSource)
    
    val getJobsUseCase = GetJobsUseCase(repository)
    val refreshJobsUseCase = RefreshJobsUseCase(repository)
    val jobView = SimpleJobView()
    
    // Настройка маршрутов
    routing {
        get("/") {
            val jobs = getJobsUseCase(JobSearchParams())
            call.respondHtml {
                jobView.renderJobsPage(this, jobs)
            }
        }
        
        get("/api/jobs") {
            val jobs = getJobsUseCase(JobSearchParams())
            call.respond(jobs)
        }
        
        post("/api/refresh") {
            val jobs = refreshJobsUseCase(JobSearchParams())
            call.respond(jobs)
        }
        
        get("/api/status") {
            val status = mapOf(
                "status" to "ok",
                "message" to "HireHi API is running"
            )
            call.respond(status)
        }
    }
}

private fun getPort(): Int {
    return System.getenv("PORT")?.toIntOrNull() ?: 10000
}