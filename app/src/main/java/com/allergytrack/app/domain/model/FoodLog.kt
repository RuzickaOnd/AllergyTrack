package com.allergytrack.app.domain.model

import java.util.UUID

data class FoodLog(
    val id: UUID = UUID.randomUUID(),
    val timestamp: Long = System.currentTimeMillis(),
    val foodName: String,
    val ingredients: List<String> = emptyList(),
    val allergens: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUri: String? = null,
    val notes: String = ""
)
