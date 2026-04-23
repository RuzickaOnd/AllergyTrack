package com.allergytrack.app.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "symptom_logs")
data class SymptomLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: String,       // SymptomType.name
    val intensity: Int,
    val bodyParts: String = "",  // JSON string
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String = ""
)
