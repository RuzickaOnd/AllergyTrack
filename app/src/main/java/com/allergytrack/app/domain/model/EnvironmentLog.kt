package com.allergytrack.app.domain.model

import java.util.UUID

data class EnvironmentLog(
    val id: UUID = UUID.randomUUID(),
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    // AQI — vždy dostupné globálně
    val aqi: Int = 0,
    val pm25: Float = 0f,
    val no2: Float = 0f,
    val ozone: Float = 0f,
    // Pyl — pouze EU a pylová sezóna, jinak null
    val pollenGrassIndex: Int? = null,
    val pollenTreeIndex: Int? = null,
    val pollenWeedIndex: Int? = null,
    // Metadata
    val region: EnvironmentRegion = EnvironmentRegion.UNKNOWN,
    val pollenAvailable: Boolean = false,
    val temperature: Float? = null,
    val humidity: Float? = null
)
