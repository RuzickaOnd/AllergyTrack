package com.allergytrack.app.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "environment_logs")
data class EnvironmentLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val aqi: Int = 0,
    val pm25: Float = 0f,
    val no2: Float = 0f,
    val ozone: Float = 0f,
    // Nullable — null = data nebyla dostupná
    val pollenGrassIndex: Int? = null,
    val pollenTreeIndex: Int? = null,
    val pollenWeedIndex: Int? = null,
    val region: String = "UNKNOWN",
    val pollenAvailable: Boolean = false,
    val temperature: Float? = null,
    val humidity: Float? = null
)
