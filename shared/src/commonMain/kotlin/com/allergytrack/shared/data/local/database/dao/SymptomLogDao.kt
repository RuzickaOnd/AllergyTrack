package com.allergytrack.shared.data.local.database.dao

import androidx.room.*
import com.allergytrack.shared.data.local.database.entity.SymptomLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomLogDao {
    @Query("SELECT * FROM symptom_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SymptomLogEntity>>

    @Query("SELECT * FROM symptom_logs WHERE timestamp >= :from AND timestamp <= :to ORDER BY timestamp DESC")
    fun getLogsBetween(from: Long, to: Long): Flow<List<SymptomLogEntity>>

    @Query("SELECT * FROM symptom_logs WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getTodayLogs(startOfDay: Long): Flow<List<SymptomLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: SymptomLogEntity)

    @Delete
    suspend fun delete(log: SymptomLogEntity)

    @Query("DELETE FROM symptom_logs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM symptom_logs")
    suspend fun deleteAll()
}
