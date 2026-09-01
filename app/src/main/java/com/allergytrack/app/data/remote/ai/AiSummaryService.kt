package com.allergytrack.app.data.remote.ai

import android.content.Context
import com.allergytrack.app.data.remote.food.FoodAllergenHelper
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
) {

    private val generativeModel: GenerativeModel by lazy {
        Generation.getClient()
    }

    suspend fun generateSummary(correlations: List<Correlation>): String = withContext(Dispatchers.Default) {
        if (correlations.isEmpty()) {
            return@withContext "Zatím nemám dostatek dat pro analýzu. Pokračuj v zaznamenávání jídla a symptomů."
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

    private fun buildPrompt(correlations: List<Correlation>): String {
        return buildString {
            append("Jsi klinický asistent pro analýzu alergií v aplikaci AllergyTrack. ")
            append("Na základě naměřených korelací mezi jídlem/prostředím a symptomy uživatele vygeneruj stručné, ")
            append("srozumitelné a personalizované shrnutí (maximálně 4-5 vět) v češtině. ")
            append("Nepoužívej úvodní fráze jako 'Zde je shrnutí'. Jdi přímo k věci. ")
            append("Důležité spouštěče a symptomy označ tučně pomocí markdownu (**text**). ")
            append("Na konci doporuč konzultaci s lékařem/alergologem.\n\n")
            append("Naměřená data:\n")
            for (corr in correlations) {
                val triggerName = localizedTriggerName(corr)
                val symptomName = context.getString(corr.symptomType.labelRes())
                val severityLabel = when (corr.severity) {
                    CorrelationSeverity.HIGH -> "vysoká"
                    CorrelationSeverity.MEDIUM -> "střední"
                    CorrelationSeverity.LOW -> "nízká"
                }
                val delayInfo = if (corr.avgDelayHours > 0) " (reakce za ${corr.avgDelayHours.toInt()}h)" else ""
                val expectedInfo = if (corr.isExpected) " [shoda s alergickým profilem]" else ""
                
                append("- $triggerName + $symptomName: korelace ${(corr.score * 100).toInt()}% ($severityLabel významnost)$delayInfo$expectedInfo\n")
            }
        }
    }

    private fun generateRuleBasedSummary(correlations: List<Correlation>): String {
        val highCorrelations = correlations.filter { it.severity == CorrelationSeverity.HIGH }
        val topTrigger = correlations.firstOrNull()

        return buildString {
            if (topTrigger != null) {
                val scorePercent = (topTrigger.score * 100).toInt()
                val triggerName = localizedTriggerName(topTrigger)
                val symptomName = context.getString(topTrigger.symptomType.labelRes())
                append("Na základě tvých záznamů jsem identifikoval hlavní spouštěč: ")
                append("**$triggerName** koreluje s $symptomName ")
                append("v $scorePercent % případů")
                if (topTrigger.avgDelayHours > 0) {
                    append(", typicky s odstupem ${topTrigger.avgDelayHours.toInt()}–${(topTrigger.avgDelayHours * 1.5).toInt()} hodin")
                }
                append(".")
            }

            if (highCorrelations.size > 1) {
                val secondName = localizedTriggerName(highCorrelations[1])
                append(" Dalším významným faktorem je $secondName.")
            }

            val foodTriggers = correlations.filter { it.triggerType == TriggerType.FOOD }
            val envTriggers = correlations.filter { it.triggerType == TriggerType.ENVIRONMENT }

            if (foodTriggers.isNotEmpty() && envTriggers.isNotEmpty()) {
                append(" Pozoroval jsem také synergický efekt — symptomy jsou intenzivnější, když se potravinový spouštěč kombinuje s vysokou hladinou pylu.")
            }

            append(" Doporučuji tato zjištění probrat s alergologem.")
        }
    }

    private fun localizedTriggerName(correlation: Correlation): String =
        if (correlation.triggerType == TriggerType.FOOD) {
            FoodAllergenHelper.keyToDisplayRes[correlation.trigger]
                ?.let { context.getString(it) }
                ?: correlation.trigger
        } else {
            when (correlation.trigger) {
                "grass_pollen" -> "travní pyl"
                "birch_pollen" -> "březový pyl"
                "pm2_5" -> "prachové částice PM2.5"
                "ozone" -> "ozón"
                "no2" -> "oxid dusičitý"
                "european_aqi" -> "index kvality ovzduší (AQI)"
                else -> correlation.trigger
            }
        }
}
