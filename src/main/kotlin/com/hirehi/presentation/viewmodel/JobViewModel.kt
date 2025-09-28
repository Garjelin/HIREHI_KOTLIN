package com.hirehi.presentation.viewmodel

import com.hirehi.domain.model.Job
import com.hirehi.domain.model.JobSearchParams
import com.hirehi.domain.usecase.GetJobsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class JobViewModel(
    private val getJobsUseCase: GetJobsUseCase
) {
    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun loadJobs(searchParams: JobSearchParams) {
        _isLoading.value = true
        _error.value = null
        
        try {
            val jobs = getJobsUseCase(searchParams)
            _jobs.value = jobs
        } catch (e: Exception) {
            _error.value = e.message ?: "Неизвестная ошибка"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
