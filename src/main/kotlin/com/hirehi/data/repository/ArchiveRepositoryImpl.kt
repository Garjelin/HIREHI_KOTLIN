package com.hirehi.data.repository

import com.hirehi.domain.model.ArchivedJob
import com.hirehi.domain.model.ArchivedJobs
import com.hirehi.domain.model.Job
import com.hirehi.domain.repository.ArchiveRepository
import com.hirehi.domain.repository.ArchiveStatistics
import com.hirehi.domain.repository.CompanyArchiveCount
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ArchiveRepositoryImpl : ArchiveRepository {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override suspend fun archiveJob(job: Job, reason: String?): Boolean {
        return try {
            transaction {
                ArchivedJobs.insert {
                    it[id] = job.id
                    it[title] = job.title
                    it[company] = job.company
                    it[salary] = job.salary
                    it[level] = job.level
                    it[format] = job.format
                    it[url] = job.url
                    it[description] = job.description
                    it[requirements] = if (job.requirements.isNotEmpty()) json.encodeToString(job.requirements) else null
                    it[benefits] = if (job.benefits.isNotEmpty()) json.encodeToString(job.benefits) else null
                    it[location] = job.location
                    it[publishedAt] = job.publishedAt
                    it[archivedAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                    it[archiveReason] = reason
                }
            }
            true
        } catch (e: Exception) {
            println("Error archiving job: ${e.message}")
            false
        }
    }
    
    override suspend fun getArchivedJobs(limit: Int, offset: Int): List<ArchivedJob> {
        return try {
            transaction {
                ArchivedJobs
                    .selectAll()
                    .orderBy(ArchivedJobs.archivedAt to SortOrder.DESC)
                    .limit(limit, offset.toLong())
                    .map { rowToArchivedJob(it) }
            }
        } catch (e: Exception) {
            println("Error getting archived jobs: ${e.message}")
            emptyList()
        }
    }
    
    override suspend fun getArchivedJobById(id: String): ArchivedJob? {
        return try {
            transaction {
                ArchivedJobs
                    .select { ArchivedJobs.id eq id }
                    .firstOrNull()
                    ?.let { rowToArchivedJob(it) }
            }
        } catch (e: Exception) {
            println("Error getting archived job by id: ${e.message}")
            null
        }
    }
    
    override suspend fun deleteArchivedJob(id: String): Boolean {
        return try {
            transaction {
                val deleted = ArchivedJobs.deleteWhere { ArchivedJobs.id eq id }
                deleted > 0
            }
        } catch (e: Exception) {
            println("Error deleting archived job: ${e.message}")
            false
        }
    }
    
    override suspend fun getArchiveStatistics(): ArchiveStatistics {
        return try {
            transaction {
                val totalArchived = ArchivedJobs.selectAll().count()
                
                // Упрощенная версия без фильтрации по месяцам пока что
                val archivedThisMonth = 0
                
                val mostArchivedCompanies = ArchivedJobs
                    .slice(ArchivedJobs.company, ArchivedJobs.id.count())
                    .selectAll()
                    .groupBy(ArchivedJobs.company)
                    .orderBy(ArchivedJobs.id.count() to SortOrder.DESC)
                    .limit(10)
                    .map { 
                        CompanyArchiveCount(
                            company = it[ArchivedJobs.company],
                            count = it[ArchivedJobs.id.count()].toInt()
                        )
                    }
                
                ArchiveStatistics(
                    totalArchived = totalArchived.toInt(),
                    archivedThisMonth = archivedThisMonth,
                    mostArchivedCompanies = mostArchivedCompanies
                )
            }
        } catch (e: Exception) {
            println("Error getting archive statistics: ${e.message}")
            ArchiveStatistics(0, 0, emptyList())
        }
    }
    
    private fun rowToArchivedJob(row: ResultRow): ArchivedJob {
        val requirements = row[ArchivedJobs.requirements]?.let { 
            try { json.decodeFromString<List<String>>(it) } catch (e: Exception) { emptyList() }
        } ?: emptyList()
        
        val benefits = row[ArchivedJobs.benefits]?.let { 
            try { json.decodeFromString<List<String>>(it) } catch (e: Exception) { emptyList() }
        } ?: emptyList()
        
        return ArchivedJob(
            id = row[ArchivedJobs.id],
            title = row[ArchivedJobs.title],
            company = row[ArchivedJobs.company],
            salary = row[ArchivedJobs.salary],
            level = row[ArchivedJobs.level],
            format = row[ArchivedJobs.format],
            url = row[ArchivedJobs.url],
            description = row[ArchivedJobs.description],
            requirements = requirements,
            benefits = benefits,
            location = row[ArchivedJobs.location],
            publishedAt = row[ArchivedJobs.publishedAt],
            archivedAt = row[ArchivedJobs.archivedAt].toString(),
            archiveReason = row[ArchivedJobs.archiveReason]
        )
    }
}