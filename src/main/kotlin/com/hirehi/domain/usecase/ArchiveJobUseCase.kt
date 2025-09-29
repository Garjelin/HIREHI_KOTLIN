package com.hirehi.domain.usecase

import com.hirehi.domain.model.Job
import com.hirehi.domain.repository.ArchiveRepository

class ArchiveJobUseCase(
    private val archiveRepository: ArchiveRepository
) {
    suspend fun execute(job: Job, reason: String? = null): Boolean {
        return archiveRepository.archiveJob(job, reason)
    }
}
