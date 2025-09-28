package com.hirehi.presentation.controller

import com.hirehi.domain.model.JobSearchParams
import com.hirehi.domain.usecase.GetJobsUseCase
import com.hirehi.domain.usecase.RefreshJobsUseCase
import com.hirehi.presentation.view.JobView
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class JobController(
    private val getJobsUseCase: GetJobsUseCase,
    private val refreshJobsUseCase: RefreshJobsUseCase,
    private val jobView: JobView
) {
    
    fun Application.configureRoutes() {
        this.routing {
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
                val jobs = getJobsUseCase(JobSearchParams())
                val status = mapOf(
                    "totalJobs" to jobs.size,
                    "lastUpdated" to System.currentTimeMillis()
                )
                call.respond(status)
            }
        }
    }
}
