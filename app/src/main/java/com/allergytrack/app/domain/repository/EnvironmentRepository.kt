package com.allergytrack.app.domain.repository

import com.allergytrack.app.domain.model.EnvironmentLog
import kotlinx.coroutines.flow.Flow

interface EnvironmentRepository {
    fun getAllLogs(): Flow<List<EnvironmentLog>>
    fun getLogsBetween(from: Long, to: Long): Flow<List<EnvironmentLog>>
    suspend fun getLatest(): EnvironmentLog?
    suspend fun saveLog(log: EnvironmentLog)
    suspend fun fetchAndSaveCurrentEnvironment(): EnvironmentLog?
}
