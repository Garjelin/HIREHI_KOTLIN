package com.hirehi.data.repository

import com.hirehi.data.remote.HireHiScraper
import com.hirehi.domain.model.Job
import com.hirehi.domain.model.JobSearchParams
import com.hirehi.domain.repository.JobRepository
import com.hirehi.domain.repository.JobScraper

class JobRepositoryImpl(
    private val scraper: JobScraper
) : JobRepository {

    override suspend fun getJobs(params: JobSearchParams): List<Job> {
        return scraper.scrapeJobs(params)
    }

    override suspend fun refreshJobs(params: JobSearchParams): List<Job> {
        return scraper.scrapeJobs(params)
    }

    override suspend fun getCachedJobs(): List<Job> {
        // В данной реализации кэширование не используется
        return emptyList()
    }

    override suspend fun saveJobs(jobs: List<Job>) {
        // В данной реализации сохранение не используется
    }

    override suspend fun getLastUpdateTime(): String? {
        return null
    }
}
