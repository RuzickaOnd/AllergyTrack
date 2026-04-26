package com.allergytrack.app.domain.model

data class Correlation(
    val trigger: String,
    val triggerType: TriggerType,
    val symptomType: SymptomType,
    val score: Float, // 0.0 - 1.0
    val occurrences: Int,
    val totalEvents: Int,
    val avgDelayHours: Float,
    val severity: CorrelationSeverity,
    val isExpected: Boolean = false
)

enum class TriggerType { FOOD, ENVIRONMENT, COMBINED }

enum class CorrelationSeverity { HIGH, MEDIUM, LOW }
