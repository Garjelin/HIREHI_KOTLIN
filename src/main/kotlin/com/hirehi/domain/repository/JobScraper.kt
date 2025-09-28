package com.hirehi.domain.repository

import com.hirehi.domain.model.Job
import com.hirehi.domain.model.JobSearchParams

interface JobScraper {
    suspend fun scrapeJobs(params: JobSearchParams): List<Job>
}
