package com.allergytrack.app.domain.repository

import com.allergytrack.app.data.remote.food.RecognizedFood
import com.allergytrack.app.domain.model.FoodLog
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    fun getAllLogs(): Flow<List<FoodLog>>
    fun getTodayLogs(): Flow<List<FoodLog>>
    suspend fun saveLog(log: FoodLog)
    suspend fun deleteLog(id: String)
    suspend fun searchFoodByName(query: String): List<RecognizedFood>
    suspend fun getFoodByBarcode(barcode: String): RecognizedFood?
    suspend fun recognizeFoodFromImage(imageBytes: ByteArray): List<RecognizedFood>
}
