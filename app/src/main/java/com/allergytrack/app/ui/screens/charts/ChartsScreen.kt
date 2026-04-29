package com.allergytrack.app.ui.screens.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.FilterVintage
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.*
import com.patrykandpatrick.vico.compose.cartesian.layer.*
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer.ColumnProvider
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer.LineFill
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.allergytrack.app.R
import com.allergytrack.app.domain.model.SymptomType
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.theme.*
import com.allergytrack.app.ui.util.labelRes
import com.allergytrack.app.domain.model.PremiumFeature

@Composable
fun ChartsScreen(
    navController: NavController,
    viewModel: ChartsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isExtendedChartsVisible by viewModel.isExtendedChartsVisible.collectAsStateWithLifecycle()
    val canUseExtendedCharts by viewModel.canUseExtendedCharts.collectAsStateWithLifecycle()
    val showExtendedChartsBadge by viewModel.showExtendedChartsBadge.collectAsStateWithLifecycle()
    val isPollenVisible by viewModel.isPollenVisible.collectAsStateWithLifecycle()
    val showPollenUpsell by viewModel.showPollenUpsell.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            if (isExtendedChartsVisible) {
                item {
                    TimeRangeSelector(
                        selected = uiState.selectedRange,
                        canUseExtendedCharts = canUseExtendedCharts,
                        showBadge = showExtendedChartsBadge,
                        onSelected = viewModel::onRangeSelected
                    )
                }
            }

            item { SummaryRow(uiState = uiState) }

            item {
                SymptomFilterRow(
                    selected = uiState.selectedSymptomFilter,
                    onSelected = viewModel::onFilterSelected
                )
            }

            uiState.chartData?.let { data ->
                item {
                    ChartCard(title = stringResource(R.string.chart_symptom_intensity), icon = Icons.Outlined.ShowChart) {
                        SymptomIntensityChart(data = data)
                    }
                }
                item {
                    PremiumGate(
                        isVisible = isPollenVisible,
                        showUpsell = showPollenUpsell,
                        feature = PremiumFeature.POLLEN_DATA
                    ) {
                        ChartCard(title = stringResource(R.string.chart_pollen_vs_symptoms), icon = Icons.Outlined.FilterVintage) {
                            PollenVsSymptomChart(data = data)
                        }
                    }
                }
                item {
                    ChartCard(title = stringResource(R.string.chart_aqi_vs_symptoms), icon = Icons.Outlined.Air) {
                        AqiVsSymptomChart(data = data)
                    }
                }
            } ?: run {
                item { EmptyChartsCard() }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun TimeRangeSelector(
    selected: TimeRange,
    canUseExtendedCharts: Boolean,
    showBadge: Boolean,
    onSelected: (TimeRange) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        TimeRange.entries.forEachIndexed { index, range ->
            val isPremiumRange = range == TimeRange.MONTH
            val isEnabled = !isPremiumRange || canUseExtendedCharts
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index, TimeRange.entries.size),
                selected = selected == range,
                onClick = { if (isEnabled) onSelected(range) },
                enabled = isEnabled,
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(stringResource(range.labelRes))
                        if (isPremiumRange && showBadge) PremiumBadge()
                    }
                }
            )
        }
    }
}

@Composable
fun SummaryRow(uiState: ChartsUiState) {
    val mostFrequentLabel = uiState.mostFrequentSymptomType
        ?.let { stringResource(it.labelRes()) }
        ?: "—"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(modifier = Modifier.weight(1f), value = uiState.totalSymptomEvents.toString(), label = stringResource(R.string.stat_total))
        StatCard(modifier = Modifier.weight(1f), value = "%.1f".format(uiState.avgDailyIntensity), label = stringResource(R.string.stat_avg))
        StatCard(modifier = Modifier.weight(1f), value = mostFrequentLabel, label = stringResource(R.string.stat_most_frequent))
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, value: String, label: String) {
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.okColor)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun SymptomFilterRow(selected: SymptomType?, onSelected: (SymptomType?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            stringResource(R.string.filter_symptoms_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = MonoFontFamily
        )
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selected == null,
                    onClick = { onSelected(null) },
                    label = { Text(stringResource(R.string.filter_all), fontFamily = MonoFontFamily, fontSize = 10.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
            items(SymptomType.entries.size) { i ->
                val type = SymptomType.entries[i]
                val label = stringResource(type.labelRes())
                FilterChip(
                    selected = selected == type,
                    onClick = { onSelected(type) },
                    label = { Text("${type.emoji} ${label.uppercase()}", fontFamily = MonoFontFamily, fontSize = 10.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@Composable
fun ChartCard(title: String, icon: ImageVector? = null, content: @Composable () -> Unit) {
    AppCard {
        CardTitle(text = title, icon = icon)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
fun SymptomIntensityChart(data: ChartDataSet) {
    if (data.symptomPoints.isEmpty() || data.symptomPoints.all { it.avgIntensity == 0f }) {
        EmptyChartPlaceholder(stringResource(R.string.chart_empty_period))
        return
    }

    val symptomColor = MaterialTheme.colorScheme.symptomColor
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries {
                series(x = data.symptomPoints.map { it.dayIndex }, y = data.symptomPoints.map { it.avgIntensity })
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnProvider.series(rememberLineComponent(fill = Fill(symptomColor), thickness = 12.dp))
            ),
            startAxis = VerticalAxis.rememberStart(valueFormatter = CartesianValueFormatter { _, value, _ -> "%.0f".format(value) }),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = CartesianValueFormatter { _, value, _ -> data.dateLabels.getOrElse(value.toInt()) { "" } })
        ),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxWidth().height(180.dp)
    )
}

@Composable
fun PollenVsSymptomChart(data: ChartDataSet) {
    val hasAnyPollenData = data.environmentPoints.any { it.pollenAvailable }

    if (!hasAnyPollenData) {
        AppCard(borderColor = MaterialTheme.colorScheme.outline) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Column {
                    Text(stringResource(R.string.pollen_unavailable_title), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    Text(stringResource(R.string.pollen_unavailable_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        return
    }

    val pollenColor = MaterialTheme.colorScheme.secondary
    val symptomColor = MaterialTheme.colorScheme.symptomColor
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            lineSeries {
                series(x = data.environmentPoints.filter { it.pollenAvailable }.map { it.dayIndex }, y = data.environmentPoints.filter { it.pollenAvailable }.map { it.pollenGrass })
                series(x = data.symptomPoints.map { it.dayIndex }, y = data.symptomPoints.map { it.avgIntensity })
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.Line(fill = LineFill.single(Fill(pollenColor))),
                    LineCartesianLayer.Line(fill = LineFill.single(Fill(symptomColor)))
                )
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = CartesianValueFormatter { _, value, _ -> data.dateLabels.getOrElse(value.toInt()) { "" } })
        ),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxWidth().height(180.dp)
    )

    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendItem(color = pollenColor, label = stringResource(R.string.legend_pollen_grass))
        LegendItem(color = symptomColor, label = stringResource(R.string.legend_symptoms))
    }
}

@Composable
fun AqiVsSymptomChart(data: ChartDataSet) {
    val aqiColor = MaterialTheme.colorScheme.okColor
    val symptomColor = MaterialTheme.colorScheme.symptomColor
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            lineSeries {
                series(x = data.environmentPoints.map { it.dayIndex }, y = data.environmentPoints.map { it.aqi })
                series(x = data.symptomPoints.map { it.dayIndex }, y = data.symptomPoints.map { it.avgIntensity })
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.Line(fill = LineFill.single(Fill(aqiColor))),
                    LineCartesianLayer.Line(fill = LineFill.single(Fill(symptomColor)))
                )
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = CartesianValueFormatter { _, value, _ -> data.dateLabels.getOrElse(value.toInt()) { "" } })
        ),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxWidth().height(180.dp)
    )

    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendItem(color = aqiColor, label = stringResource(R.string.legend_aqi))
        LegendItem(color = symptomColor, label = stringResource(R.string.legend_symptoms))
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(modifier = Modifier.size(10.dp), color = color, shape = MaterialTheme.shapes.extraSmall) {}
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun EmptyChartPlaceholder(message: String) {
    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun EmptyChartsCard() {
    AppCard(borderColor = MaterialTheme.colorScheme.outline) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("📊", style = androidx.compose.ui.text.TextStyle(fontSize = 40.sp))
            Text(stringResource(R.string.empty_charts_title), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                stringResource(R.string.empty_charts_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
