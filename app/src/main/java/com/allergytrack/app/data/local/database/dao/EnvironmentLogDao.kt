package com.allergytrack.app.data.local.database.dao

import androidx.room.*
import com.allergytrack.app.data.local.database.entity.EnvironmentLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EnvironmentLogDao {
    @Query("SELECT * FROM environment_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<EnvironmentLogEntity>>

    @Query("SELECT * FROM environment_logs WHERE timestamp >= :from AND timestamp <= :to ORDER BY timestamp ASC")
    fun getLogsBetween(from: Long, to: Long): Flow<List<EnvironmentLogEntity>>

    @Query("SELECT * FROM environment_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(): EnvironmentLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: EnvironmentLogEntity)

    @Query("DELETE FROM environment_logs")
    suspend fun deleteAll()
}
