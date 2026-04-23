package com.allergytrack.app.ui.screens.charts

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.cartesian.data.*
import com.patrykandpatrick.vico.compose.cartesian.layer.*
import com.patrykandpatrick.vico.compose.cartesian.axis.*
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer.ColumnProvider
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer.LineFill
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.domain.model.PremiumFeature
import com.allergytrack.app.domain.model.SymptomType
import com.allergytrack.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        containerColor = Background
    ) { padding ->

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentGreen)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            SectionHeading(
                title = "Analytics",
                subtitle = "Trends and environmental correlation"
            )

            // Time range selector
            if (isExtendedChartsVisible) {
                TimeRangeSelector(
                    selected = uiState.selectedRange,
                    canUseExtendedCharts = canUseExtendedCharts,
                    showBadge = showExtendedChartsBadge,
                    onSelected = viewModel::onRangeSelected
                )
            }

            // Summary stats
            SummaryRow(uiState = uiState)

            // Symptom filter
            SymptomFilterRow(
                selected = uiState.selectedSymptomFilter,
                onSelected = viewModel::onFilterSelected
            )

            // Chart 1: Daily symptom intensity
            uiState.chartData?.let { data ->
                ChartCard(title = "SYMPTOM INTENSITY") {
                    SymptomIntensityChart(data = data)
                }

                // Chart 2: Pollen vs. symptoms
                PremiumGate(
                    isVisible = isPollenVisible,
                    showUpsell = showPollenUpsell,
                    feature = PremiumFeature.POLLEN_DATA
                ) {
                    ChartCard(title = "POLLEN VS. SYMPTOMS") {
                        PollenVsSymptomChart(data = data)
                    }
                }

                // Chart 3: AQI vs. symptoms
                ChartCard(title = "AIR QUALITY (AQI) VS. SYMPTOMS") {
                    AqiVsSymptomChart(data = data)
                }
            } ?: run {
                EmptyChartsCard()
            }

            Spacer(Modifier.height(100.dp))
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
                        Text(range.label)
                        if (isPremiumRange && showBadge) {
                            PremiumBadge()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun SummaryRow(uiState: ChartsUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            value = uiState.totalSymptomEvents.toString(),
            label = "TOTAL LOGS"
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = "%.1f".format(uiState.avgDailyIntensity),
            label = "AVG SEVERITY"
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = uiState.mostFrequentSymptom,
            label = "FREQUENT"
        )
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, value: String, label: String) {
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = AccentGreen
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = TextDim,
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SymptomFilterRow(selected: SymptomType?, onSelected: (SymptomType?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Symptom Filter", style = MaterialTheme.typography.labelSmall,
            color = TextDim, fontFamily = MonoFontFamily)
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selected == null,
                    onClick = { onSelected(null) },
                    label = { Text("ALL", fontFamily = MonoFontFamily, fontSize = 10.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentBlue.copy(alpha = 0.2f),
                        selectedLabelColor = AccentBlue
                    )
                )
            }
            items(SymptomType.entries.size) { i ->
                val type = SymptomType.entries[i]
                FilterChip(
                    selected = selected == type,
                    onClick = { onSelected(type) },
                    label = { Text("${type.emoji} ${type.label.uppercase()}", fontFamily = MonoFontFamily, fontSize = 10.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentBlue.copy(alpha = 0.2f),
                        selectedLabelColor = AccentBlue
                    )
                )
            }
        }
    }
}

@Composable
fun ChartCard(title: String, content: @Composable () -> Unit) {
    AppCard {
        CardTitle(text = title)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
fun SymptomIntensityChart(data: ChartDataSet) {
    if (data.symptomPoints.isEmpty() || data.symptomPoints.all { it.avgIntensity == 0f }) {
        EmptyChartPlaceholder("No data for this period")
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries {
                series(
                    x = data.symptomPoints.map { it.dayIndex },
                    y = data.symptomPoints.map { it.avgIntensity }
                )
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnProvider.series(
                    rememberLineComponent(
                        fill = Fill(AccentOrange),
                        thickness = 12.dp
                    )
                )
            ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = CartesianValueFormatter { _, value, _ -> "%.0f".format(value) }
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    data.dateLabels.getOrElse(value.toInt()) { "" }
                }
            )
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    )
}

@Composable
fun PollenVsSymptomChart(data: ChartDataSet) {

    // Pokud žádný den neměl pylová data, zobraz informaci místo prázdného grafu
    val hasAnyPollenData = data.environmentPoints.any { it.pollenAvailable }

    if (!hasAnyPollenData) {
        AppCard(borderColor = Border) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = TextSecondary
                )
                Column {
                    Text(
                        "Environmental Data Unavailable",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                    Text(
                        "Pollen data is not available for this region or season (available Mar–Sep in EU).",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                }
            }
        }
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            lineSeries {
                // Pyl trávy
                series(
                    x = data.environmentPoints.filter { it.pollenAvailable }.map { it.dayIndex },
                    y = data.environmentPoints.filter { it.pollenAvailable }.map { it.pollenGrass }
                )
                // Symptomy
                series(
                    x = data.symptomPoints.map { it.dayIndex },
                    y = data.symptomPoints.map { it.avgIntensity }
                )
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.Line(
                        fill = LineFill.single(Fill(AccentBlue))
                    ),
                    LineCartesianLayer.Line(
                        fill = LineFill.single(Fill(AccentOrange))
                    )
                )
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    data.dateLabels.getOrElse(value.toInt()) { "" }
                }
            )
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    )

    // Legenda
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LegendItem(color = AccentBlue, label = "Pollen (Grass)")
        LegendItem(color = AccentOrange, label = "Symptoms")
    }
}

@Composable
fun AqiVsSymptomChart(data: ChartDataSet) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = data.environmentPoints.map { it.dayIndex },
                    y = data.environmentPoints.map { it.aqi }
                )
                series(
                    x = data.symptomPoints.map { it.dayIndex },
                    y = data.symptomPoints.map { it.avgIntensity }
                )
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.Line(
                        fill = LineFill.single(Fill(AccentGreen))
                    ),
                    LineCartesianLayer.Line(
                        fill = LineFill.single(Fill(AccentOrange))
                    )
                )
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    data.dateLabels.getOrElse(value.toInt()) { "" }
                }
            )
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LegendItem(color = AccentGreen, label = "AQI (Norm.)")
        LegendItem(color = AccentOrange, label = "Symptoms")
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            modifier = Modifier.size(10.dp),
            color = color,
            shape = MaterialTheme.shapes.extraSmall
        ) {}
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun EmptyChartPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun EmptyChartsCard() {
    AppCard(borderColor = Border) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("📊", style = androidx.compose.ui.text.TextStyle(fontSize = 40.sp))
            Text(
                "Insufficient Data",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Text(
                "Log your food and symptoms daily. Charts will appear once we have enough entries to visualize trends.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
