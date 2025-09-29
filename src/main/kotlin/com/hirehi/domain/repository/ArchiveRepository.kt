package com.hirehi.domain.repository

import com.hirehi.domain.model.ArchivedJob
import kotlinx.serialization.Serializable

interface ArchiveRepository {
    suspend fun archiveJob(job: com.hirehi.domain.model.Job, reason: String? = null): Boolean
    suspend fun getArchivedJobs(limit: Int = 100, offset: Int = 0): List<ArchivedJob>
    suspend fun getArchivedJobById(id: String): ArchivedJob?
    suspend fun deleteArchivedJob(id: String): Boolean
    suspend fun getArchiveStatistics(): ArchiveStatistics
}

@Serializable
data class ArchiveStatistics(
    val totalArchived: Int,
    val archivedThisMonth: Int,
    val mostArchivedCompanies: List<CompanyArchiveCount>
)

@Serializable
data class CompanyArchiveCount(
    val company: String,
    val count: Int
)
