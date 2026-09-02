package com.allergytrack.shared.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.allergytrack.shared.platform.currentTimeMillis
import com.allergytrack.shared.platform.randomUuidString

@Entity(tableName = "environment_logs")
data class EnvironmentLogEntity(
    @PrimaryKey
    val id: String = randomUuidString(),
    val timestamp: Long = currentTimeMillis(),
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
