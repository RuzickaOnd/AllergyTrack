package com.allergytrack.app.data.remote.ai

import com.allergytrack.shared.resources.*
import com.allergytrack.shared.platform.AiSummaryGenerator
import org.jetbrains.compose.resources.getString
import android.content.Context
import com.allergytrack.shared.data.remote.food.FoodAllergenHelper
import com.allergytrack.shared.domain.model.Correlation
import com.allergytrack.shared.domain.model.CorrelationSeverity
import com.allergytrack.shared.domain.model.TriggerType
import com.allergytrack.app.ui.util.labelRes

import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.GenerateContentRequest
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.common.FeatureStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class AiSummaryService(
    private val context: Context
) : AiSummaryGenerator {

    private val generativeModel: GenerativeModel by lazy {
        Generation.getClient()
    }

    override suspend fun generateSummary(correlations: List<Correlation>): String = withContext(Dispatchers.Default) {
        if (correlations.isEmpty()) {
            return@withContext getString(Res.string.ai_summary_not_enough_data)
        }

        try {
            val status = generativeModel.checkStatus()
            if (status == FeatureStatus.AVAILABLE) {
                val prompt = buildPrompt(correlations)
                val request = GenerateContentRequest.Builder(TextPart(prompt)).build()
                val response = generativeModel.generateContent(request)
                val text = response.candidates.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    return@withContext text.trim()
                }
            } else {
                Log.d("AiSummaryService", "Gemini Nano status is not AVAILABLE (status: $status). Using fallback.")
            }
        } catch (e: Exception) {
            Log.e("AiSummaryService", "Error generating summary with Gemini Nano, using fallback.", e)
        }

        return@withContext generateRuleBasedSummary(correlations)
    }

    /**
     * Prompt se skládá z resources včetně instrukce o jazyce
     * (`ai_prompt_language`), takže model odpovídá jazykem aplikace
     * a jazyk není potřeba nikde detekovat.
     */
    private suspend fun buildPrompt(correlations: List<Correlation>): String {
        return buildString {
            append(getString(Res.string.ai_prompt_role))
            append(getString(Res.string.ai_prompt_task))
            append(getString(Res.string.ai_prompt_style, getString(Res.string.ai_prompt_language)))
            append(getString(Res.string.ai_prompt_no_preamble))
            append(getString(Res.string.ai_prompt_markdown))
            append(getString(Res.string.ai_prompt_closing))
            append("\n\n")
            append(getString(Res.string.ai_prompt_data_header))
            append("\n")
            for (corr in correlations) {
                val severityLabel = getString(
                    when (corr.severity) {
                        CorrelationSeverity.HIGH -> Res.string.ai_severity_high
                        CorrelationSeverity.MEDIUM -> Res.string.ai_severity_medium
                        CorrelationSeverity.LOW -> Res.string.ai_severity_low
                    }
                )
                val delayInfo = if (corr.avgDelayHours > 0)
                    getString(Res.string.ai_prompt_delay, corr.avgDelayHours.toInt()) else ""
                val expectedInfo = if (corr.isExpected)
                    getString(Res.string.ai_prompt_expected) else ""

                append(
                    getString(
                        Res.string.ai_prompt_data_row,
                        localizedTriggerName(corr),
                        getString(corr.symptomType.labelRes()),
                        (corr.score * 100).toInt(),
                        severityLabel,
                        delayInfo,
                        expectedInfo
                    )
                )
                append("\n")
            }
        }
    }

    private suspend fun generateRuleBasedSummary(correlations: List<Correlation>): String {
        val highCorrelations = correlations.filter { it.severity == CorrelationSeverity.HIGH }
        val topTrigger = correlations.firstOrNull()

        return buildString {
            if (topTrigger != null) {
                append(
                    getString(
                        Res.string.ai_summary_main_trigger,
                        localizedTriggerName(topTrigger),
                        getString(topTrigger.symptomType.labelRes()),
                        (topTrigger.score * 100).toInt()
                    )
                )
                if (topTrigger.avgDelayHours > 0) {
                    append(
                        getString(
                            Res.string.ai_summary_delay,
                            topTrigger.avgDelayHours.toInt(),
                            (topTrigger.avgDelayHours * 1.5).toInt()
                        )
                    )
                }
                append(".")
            }

            if (highCorrelations.size > 1) {
                append(
                    getString(
                        Res.string.ai_summary_second_factor,
                        localizedTriggerName(highCorrelations[1])
                    )
                )
            }

            val foodTriggers = correlations.filter { it.triggerType == TriggerType.FOOD }
            val envTriggers = correlations.filter { it.triggerType == TriggerType.ENVIRONMENT }

            if (foodTriggers.isNotEmpty() && envTriggers.isNotEmpty()) {
                append(getString(Res.string.ai_summary_synergy))
            }

            append(getString(Res.string.ai_summary_consult))
        }
    }

    private suspend fun localizedTriggerName(correlation: Correlation): String =
        if (correlation.triggerType == TriggerType.FOOD) {
            FoodAllergenHelper.keyToDisplayRes[correlation.trigger]
                ?.let { getString(it) }
                ?: correlation.trigger
        } else {
            when (correlation.trigger) {
                "grass_pollen" -> getString(Res.string.trigger_grass_pollen)
                "birch_pollen" -> getString(Res.string.trigger_birch_pollen)
                "pm2_5" -> getString(Res.string.trigger_pm25)
                "ozone" -> getString(Res.string.trigger_ozone)
                "no2" -> getString(Res.string.trigger_no2)
                "european_aqi" -> getString(Res.string.trigger_aqi)
                else -> correlation.trigger
            }
        }
}
