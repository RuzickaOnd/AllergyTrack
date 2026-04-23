package com.allergytrack.app.domain.model

import java.util.UUID

data class SymptomLog(
    val id: UUID = UUID.randomUUID(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: SymptomType,
    val intensity: Int, // 1-5
    val bodyParts: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String = ""
)
