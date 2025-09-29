package com.hirehi.domain.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.time.Instant

@Serializable
data class ArchivedJob(
    val id: String,
    val title: String,
    val company: String,
    val salary: String?,
    val level: String,
    val format: String,
    val url: String,
    val description: String? = null,
    val requirements: List<String> = emptyList(),
    val benefits: List<String> = emptyList(),
    val location: String? = null,
    val publishedAt: String? = null,
    val archivedAt: String,
    val archiveReason: String? = null
)

object ArchivedJobs : Table("archived_jobs") {
    val id = varchar("id", 255)
    val title = varchar("title", 500)
    val company = varchar("company", 255)
    val salary = varchar("salary", 255).nullable()
    val level = varchar("level", 100)
    val format = varchar("format", 100)
    val url = text("url")
    val description = text("description").nullable()
    val requirements = text("requirements").nullable() // JSON string
    val benefits = text("benefits").nullable() // JSON string
    val location = varchar("location", 255).nullable()
    val publishedAt = varchar("published_at", 100).nullable()
    val archivedAt = datetime("archived_at")
    val archiveReason = varchar("archive_reason", 500).nullable()
    
    override val primaryKey = PrimaryKey(id)
}
