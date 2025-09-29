package com.hirehi.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ArchiveRequest(
    val jobId: String,
    val reason: String? = null
)
