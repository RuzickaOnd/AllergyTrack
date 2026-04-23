package com.allergytrack.app.data.local.database.dao

import androidx.room.*
import com.allergytrack.app.data.local.database.entity.FoodLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {
    @Query("SELECT * FROM food_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<FoodLogEntity>>

    @Query("SELECT * FROM food_logs WHERE timestamp >= :from AND timestamp <= :to ORDER BY timestamp DESC")
    fun getLogsBetween(from: Long, to: Long): Flow<List<FoodLogEntity>>

    @Query("SELECT * FROM food_logs WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getTodayLogs(startOfDay: Long): Flow<List<FoodLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: FoodLogEntity)

    @Delete
    suspend fun delete(log: FoodLogEntity)

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteById(id: String)
}
