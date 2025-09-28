package com.hirehi.domain.repository

import com.hirehi.domain.model.Job
import com.hirehi.domain.model.JobSearchParams

interface JobRepository {
    suspend fun getJobs(params: JobSearchParams): List<Job>
    suspend fun refreshJobs(params: JobSearchParams): List<Job>
    suspend fun getCachedJobs(): List<Job>
    suspend fun saveJobs(jobs: List<Job>)
    suspend fun getLastUpdateTime(): String?
}
