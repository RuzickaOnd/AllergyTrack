package com.allergytrack.shared.domain.model

import com.allergytrack.shared.platform.currentTimeMillis
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class SymptomLog(
    val id: Uuid = Uuid.random(),
    val timestamp: Long = currentTimeMillis(),
    val type: SymptomType,
    val intensity: Int, // 1-5
    val bodyParts: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String = ""
)
