package com.hirehi.data.local

import com.hirehi.domain.model.Job
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class JobLocalDataSource {
    
    private val jobsFile = File("hirehi_jobs.json")
    private val lastUpdateFile = File("last_update.txt")
    private val json = Json { prettyPrint = true }
    
    suspend fun getJobs(): List<Job> {
        return try {
            if (jobsFile.exists()) {
                val jsonString = jobsFile.readText()
                json.decodeFromString<List<Job>>(jsonString)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Error reading jobs from file: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun saveJobs(jobs: List<Job>) {
        try {
            val jsonString = json.encodeToString(jobs)
            jobsFile.writeText(jsonString)
        } catch (e: Exception) {
            println("Error saving jobs to file: ${e.message}")
        }
    }
    
    suspend fun getLastUpdateTime(): String? {
        return try {
            if (lastUpdateFile.exists()) {
                lastUpdateFile.readText().trim()
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error reading last update time: ${e.message}")
            null
        }
    }
    
    suspend fun updateLastUpdateTime() {
        try {
            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            lastUpdateFile.writeText(currentTime)
        } catch (e: Exception) {
            println("Error updating last update time: ${e.message}")
        }
    }
}
