package com.hirehi.domain.usecase

import com.hirehi.domain.model.ArchivedJob
import com.hirehi.domain.repository.ArchiveRepository

class GetArchivedJobsUseCase(
    private val archiveRepository: ArchiveRepository
) {
    suspend fun execute(limit: Int = 100, offset: Int = 0): List<ArchivedJob> {
        return archiveRepository.getArchivedJobs(limit, offset)
    }
}
