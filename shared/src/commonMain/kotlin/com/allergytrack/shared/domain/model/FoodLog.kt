package com.allergytrack.shared.domain.model

import com.allergytrack.shared.platform.currentTimeMillis
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class FoodLog(
    val id: Uuid = Uuid.random(),
    val timestamp: Long = currentTimeMillis(),
    val foodName: String,
    val ingredients: List<String> = emptyList(),
    val allergens: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUri: String? = null,
    val notes: String = ""
)
