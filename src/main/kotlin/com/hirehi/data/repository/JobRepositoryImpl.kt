package com.hirehi.data.repository

import com.hirehi.data.local.JobLocalDataSource
import com.hirehi.data.remote.HireHiScraper
import com.hirehi.domain.model.Job
import com.hirehi.domain.model.JobSearchParams
import com.hirehi.domain.repository.JobRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class JobRepositoryImpl(
    private val scraper: HireHiScraper,
    private val localDataSource: JobLocalDataSource
) : JobRepository {
    
    override suspend fun getJobs(params: JobSearchParams): List<Job> {
        val cachedJobs = localDataSource.getJobs()
        
        // Если кэш пустой или устарел (старше 1 часа), обновляем данные
        val lastUpdate = localDataSource.getLastUpdateTime()
        val shouldRefresh = cachedJobs.isEmpty() || isCacheExpired(lastUpdate)
        
        return if (shouldRefresh) {
            refreshJobs(params)
        } else {
            cachedJobs
        }
    }
    
    override suspend fun refreshJobs(params: JobSearchParams): List<Job> {
        return try {
            val jobs = scraper.scrapeJobs(params)
            localDataSource.saveJobs(jobs)
            localDataSource.updateLastUpdateTime()
            jobs
        } catch (e: Exception) {
            println("Error refreshing jobs: ${e.message}")
            // Возвращаем кэшированные данные в случае ошибки
            localDataSource.getJobs()
        }
    }
    
    override suspend fun getCachedJobs(): List<Job> {
        return localDataSource.getJobs()
    }
    
    override suspend fun saveJobs(jobs: List<Job>) {
        localDataSource.saveJobs(jobs)
        localDataSource.updateLastUpdateTime()
    }
    
    override suspend fun getLastUpdateTime(): String? {
        return localDataSource.getLastUpdateTime()
    }
    
    private fun isCacheExpired(lastUpdate: String?): Boolean {
        if (lastUpdate == null) return true
        
        return try {
            val lastUpdateTime = LocalDateTime.parse(lastUpdate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val now = LocalDateTime.now()
            val hoursDiff = java.time.Duration.between(lastUpdateTime, now).toHours()
            hoursDiff >= 1 // Кэш истекает через 1 час
        } catch (e: Exception) {
            true // Если не можем распарсить время, считаем кэш устаревшим
        }
    }
}
