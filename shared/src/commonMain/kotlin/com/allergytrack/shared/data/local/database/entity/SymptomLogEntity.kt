package com.allergytrack.shared.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.allergytrack.shared.platform.currentTimeMillis
import com.allergytrack.shared.platform.randomUuidString

@Entity(tableName = "symptom_logs")
data class SymptomLogEntity(
    @PrimaryKey
    val id: String = randomUuidString(),
    val timestamp: Long = currentTimeMillis(),
    val type: String,       // SymptomType.name
    val intensity: Int,
    val bodyParts: String = "",  // JSON string
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String = ""
)
