package com.hirehi.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ArchiveResponse(
    val jobs: List<ArchivedJob>,
    val total: Int,
    val limit: Int,
    val offset: Int
)
