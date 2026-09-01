package com.allergytrack.app.ui.screens.charts

import com.allergytrack.shared.data.local.database.entity.EnvironmentLogEntity
import com.allergytrack.shared.data.local.database.entity.SymptomLogEntity
import com.allergytrack.shared.domain.model.SymptomType
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class DailySymptomPoint(
    val dayIndex: Float,
    val avgIntensity: Float
)

data class DailyEnvironmentPoint(
    val dayIndex: Float,
    val pollenGrass: Float,    // -1f = nedostupné, jinak 0–5 normalizováno
    val aqi: Float,            // 0–5 normalizováno dynamicky
    val pollenAvailable: Boolean
)

data class ChartDataSet(
    val symptomPoints: List<DailySymptomPoint>,
    val environmentPoints: List<DailyEnvironmentPoint>,
    val dateLabels: List<String>
)

object ChartDataMapper {

    fun mapToChartData(
        symptomLogs: List<SymptomLogEntity>,
        environmentLogs: List<EnvironmentLogEntity>,
        filterType: SymptomType? = null,
        days: Int = 7
    ): ChartDataSet {
        val now = System.currentTimeMillis()
        val from = now - TimeUnit.DAYS.toMillis(days.toLong())

        val symptomsByDay = symptomLogs
            .filter { it.timestamp >= from }
            .filter { filterType == null || it.type == filterType.name }
            .groupBy { dayIndex(it.timestamp, from) }

        val envByDay = environmentLogs
            .filter { it.timestamp >= from }
            .groupBy { dayIndex(it.timestamp, from) }

        val dateLabels = (0 until days).map { i ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = from + TimeUnit.DAYS.toMillis(i.toLong())
            "${cal.get(Calendar.DAY_OF_MONTH)}.${cal.get(Calendar.MONTH) + 1}"
        }

        // Normalizační konstanty z celého datasetu — zajistí, že AQI a pyl
        // budou vždy na škále 0–5 srovnatelné s intenzitou příznaků.
        val maxAqi = environmentLogs
            .maxOfOrNull { it.aqi.toFloat() }
            ?.coerceAtLeast(20f) ?: 100f
        val maxPollen = environmentLogs
            .mapNotNull { it.pollenGrassIndex }
            .maxOrNull()?.toFloat()
            ?.coerceAtLeast(10f) ?: 100f

        val symptomPoints = (0 until days).map { i ->
            val dayLogs = symptomsByDay[i] ?: emptyList()
            DailySymptomPoint(
                dayIndex = i.toFloat(),
                avgIntensity = if (dayLogs.isEmpty()) 0f
                               else dayLogs.map { it.intensity }.average().toFloat()
            )
        }

        val envPoints = (0 until days).map { i ->
            val dayLogs = envByDay[i] ?: emptyList()
            val pollenValues = dayLogs.mapNotNull { it.pollenGrassIndex }
            DailyEnvironmentPoint(
                dayIndex = i.toFloat(),
                pollenGrass = if (pollenValues.isEmpty()) -1f
                              else (pollenValues.average().toFloat() / maxPollen * 5f).coerceIn(0f, 5f),
                aqi = if (dayLogs.isEmpty()) 0f
                      else (dayLogs.map { it.aqi }.average().toFloat() / maxAqi * 5f).coerceIn(0f, 5f),
                pollenAvailable = dayLogs.any { it.pollenAvailable }
            )
        }

        return ChartDataSet(
            symptomPoints = symptomPoints,
            environmentPoints = envPoints,
            dateLabels = dateLabels
        )
    }

    private fun dayIndex(timestamp: Long, from: Long): Int =
        TimeUnit.MILLISECONDS.toDays(timestamp - from).toInt().coerceIn(0, 30)
}
