package com.allergytrack.app.data.remote.ai

import android.content.Context
import com.allergytrack.app.data.remote.food.FoodAllergenHelper
import com.allergytrack.app.domain.model.Correlation
import com.allergytrack.app.domain.model.CorrelationSeverity
import com.allergytrack.app.domain.model.TriggerType
import com.allergytrack.app.ui.util.labelRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiSummaryService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // MVP: Generování shrnutí lokálně bez API klíče
    // V produkci nahradit voláním Claude API
    fun generateSummary(correlations: List<Correlation>): String {
        if (correlations.isEmpty()) {
            return "Zatím nemám dostatek dat pro analýzu. Pokračuj v zaznamenávání jídla a symptomů."
        }

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
            correlation.trigger
        }
}
