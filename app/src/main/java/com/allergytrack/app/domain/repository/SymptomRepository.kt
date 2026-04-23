package com.allergytrack.app.domain.repository

import com.allergytrack.app.domain.model.SymptomLog
import kotlinx.coroutines.flow.Flow

interface SymptomRepository {
    fun getAllLogs(): Flow<List<SymptomLog>>
    fun getTodayLogs(): Flow<List<SymptomLog>>
    fun getLogsBetween(from: Long, to: Long): Flow<List<SymptomLog>>
    suspend fun saveLog(log: SymptomLog)
    suspend fun deleteLog(id: String)
}
