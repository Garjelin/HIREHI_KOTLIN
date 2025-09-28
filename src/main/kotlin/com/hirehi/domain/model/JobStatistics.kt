package com.hirehi.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class JobStatistics(
    val totalJobs: Int,
    val filteredJobs: Int,
    val lastUpdated: String,
    val keywords: List<String>
)
