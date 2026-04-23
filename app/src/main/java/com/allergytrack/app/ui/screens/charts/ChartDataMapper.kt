package com.allergytrack.app.ui.screens.charts

import com.allergytrack.app.data.local.database.entity.EnvironmentLogEntity
import com.allergytrack.app.data.local.database.entity.SymptomLogEntity
import com.allergytrack.app.domain.model.SymptomType
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class DailySymptomPoint(
    val dayIndex: Float,       // 0 = nejstarší den
    val avgIntensity: Float,
    val date: Long
)

data class DailyEnvironmentPoint(
    val dayIndex: Float,
    val pollenGrass: Float,   // -1f = nedostupné
    val aqi: Float,
    val pollenAvailable: Boolean,
    val date: Long
)

data class ChartDataSet(
    val symptomPoints: List<DailySymptomPoint>,
    val environmentPoints: List<DailyEnvironmentPoint>,
    val dateLabels: List<String>,
    val maxSymptomValue: Float,
    val maxEnvValue: Float
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

        // Group symptoms by day
        val symptomsByDay = symptomLogs
            .filter { it.timestamp >= from }
            .filter { filterType == null || it.type == filterType.name }
            .groupBy { dayIndex(it.timestamp, from) }

        // Group environment by day
        val envByDay = environmentLogs
            .filter { it.timestamp >= from }
            .groupBy { dayIndex(it.timestamp, from) }

        val dateLabels = (0 until days).map { i ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = from + TimeUnit.DAYS.toMillis(i.toLong())
            "${cal.get(Calendar.DAY_OF_MONTH)}.${cal.get(Calendar.MONTH) + 1}"
        }

        val symptomPoints = (0 until days).map { i ->
            val dayLogs = symptomsByDay[i] ?: emptyList()
            DailySymptomPoint(
                dayIndex = i.toFloat(),
                avgIntensity = if (dayLogs.isEmpty()) 0f
                               else dayLogs.map { it.intensity }.average().toFloat(),
                date = from + TimeUnit.DAYS.toMillis(i.toLong())
            )
        }

        val envPoints = (0 until days).map { i ->
            val dayLogs = envByDay[i] ?: emptyList()
            DailyEnvironmentPoint(
                dayIndex = i.toFloat(),
                // pollenGrass: průměr pouze z logů kde byl pyl dostupný
                pollenGrass = dayLogs
                    .mapNotNull { it.pollenGrassIndex }  // null hodnoty ignorujeme
                    .let { values ->
                        if (values.isEmpty()) -1f         // -1 = data nebyla dostupná
                        else values.average().toFloat()
                    },
                aqi = if (dayLogs.isEmpty()) 0f
                      else dayLogs.map { it.aqi }.average().toFloat() / 20f, // normalize 0-5
                pollenAvailable = dayLogs.any { it.pollenAvailable },
                date = from + TimeUnit.DAYS.toMillis(i.toLong())
            )
        }

        return ChartDataSet(
            symptomPoints = symptomPoints,
            environmentPoints = envPoints,
            dateLabels = dateLabels,
            maxSymptomValue = 5f,
            maxEnvValue = 5f
        )
    }

    private fun dayIndex(timestamp: Long, from: Long): Int =
        TimeUnit.MILLISECONDS.toDays(timestamp - from).toInt().coerceIn(0, 30)
}
