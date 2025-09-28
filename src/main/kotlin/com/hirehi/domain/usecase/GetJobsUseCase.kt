package com.hirehi.domain.usecase

import com.hirehi.domain.model.Job
import com.hirehi.domain.model.JobSearchParams
import com.hirehi.domain.repository.JobRepository

class GetJobsUseCase(
    private val jobRepository: JobRepository
) {
    suspend operator fun invoke(params: JobSearchParams): List<Job> {
        return jobRepository.getJobs(params)
    }
}
