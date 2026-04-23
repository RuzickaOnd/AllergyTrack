package com.allergytrack.app.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "food_logs")
data class FoodLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val foodName: String,
    val ingredients: String = "",      // JSON string
    val allergens: String = "",        // JSON string
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUri: String? = null,
    val notes: String = ""
)
