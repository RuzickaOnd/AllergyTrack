package com.allergytrack.app.data.remote.ai

import com.allergytrack.app.domain.model.Correlation
import com.allergytrack.app.domain.model.TriggerType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiSummaryService @Inject constructor() {

    // MVP: Generování shrnutí lokálně bez API klíče
    // V produkci nahradit voláním Claude API nebo OpenAI
    fun generateSummary(correlations: List<Correlation>): String {
        if (correlations.isEmpty()) {
            return "Zatím nemám dostatek dat pro analýzu. Pokračuj v zaznamenávání jídla a symptomů."
        }

        val highCorrelations = correlations.filter { it.severity == com.allergytrack.app.domain.model.CorrelationSeverity.HIGH }
        val topTrigger = correlations.firstOrNull()

        return buildString {
            if (topTrigger != null) {
                val scorePercent = (topTrigger.score * 100).toInt()
                append("Na základě tvých záznamů jsem identifikoval hlavní spouštěč: ")
                append("**${topTrigger.trigger}** koreluje s ${topTrigger.symptomType.label} ")
                append("v $scorePercent % případů")
                if (topTrigger.avgDelayHours > 0) {
                    append(", typicky s odstupem ${topTrigger.avgDelayHours.toInt()}–${(topTrigger.avgDelayHours * 1.5).toInt()} hodin")
                }
                append(".")
            }

            if (highCorrelations.size > 1) {
                append(" Dalším významným faktorem je ${highCorrelations[1].trigger}.")
            }

            val foodTriggers = correlations.filter { it.triggerType == TriggerType.FOOD }
            val envTriggers = correlations.filter { it.triggerType == TriggerType.ENVIRONMENT }

            if (foodTriggers.isNotEmpty() && envTriggers.isNotEmpty()) {
                append(" Pozoroval jsem také synergický efekt — symptomy jsou intenzivnější, když se potravinový spouštěč kombinuje s vysokou hladinou pylu.")
            }

            append(" Doporučuji tato zjištění probrat s alergologem.")
        }
    }
}
