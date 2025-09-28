package com.hirehi.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Job(
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
    val publishedAt: String? = null
)

@Serializable
data class JobSearchParams(
    val category: String = "qa",
    val format: String = "удалённо",
    val levels: List<String> = listOf("senior", "middle"),
    val subcategory: String = "auto",
    val keywords: List<String> = listOf("Kotlin", "Android")
)

@Serializable
data class JobResponse(
    val jobs: List<Job>,
    val totalCount: Int,
    val lastUpdated: String
)
