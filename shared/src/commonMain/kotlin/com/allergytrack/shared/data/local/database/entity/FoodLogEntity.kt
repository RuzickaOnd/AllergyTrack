package com.allergytrack.shared.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.allergytrack.shared.platform.currentTimeMillis
import com.allergytrack.shared.platform.randomUuidString

@Entity(tableName = "food_logs")
data class FoodLogEntity(
    @PrimaryKey
    val id: String = randomUuidString(),
    val timestamp: Long = currentTimeMillis(),
    val foodName: String,
    val ingredients: String = "",      // JSON string
    val allergens: String = "",        // JSON string
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUri: String? = null,
    val notes: String = ""
)
