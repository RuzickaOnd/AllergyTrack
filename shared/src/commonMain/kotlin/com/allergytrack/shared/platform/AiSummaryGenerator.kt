package com.allergytrack.shared.platform

import com.allergytrack.shared.domain.model.Correlation

/** Implementuje AiSummaryService v :app (ML Kit GenAI / Gemini Nano). */
interface AiSummaryGenerator {
    suspend fun generateSummary(correlations: List<Correlation>): String
}
