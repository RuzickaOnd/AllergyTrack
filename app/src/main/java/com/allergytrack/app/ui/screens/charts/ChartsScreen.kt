package com.allergytrack.app.ui.screens.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.FilterVintage
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import com.allergytrack.app.ui.screens.profile.PremiumActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.allergytrack.app.ui.navigation.Screen
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.*
import com.patrykandpatrick.vico.compose.cartesian.layer.*
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer.ColumnProvider
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer.LineFill
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.VicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.allergytrack.shared.resources.*
import com.allergytrack.shared.domain.model.SymptomType
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.theme.*
import com.allergytrack.app.ui.util.icon
import com.allergytrack.app.ui.util.labelRes
import com.allergytrack.shared.domain.model.PremiumFeature
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan

@Composable
fun ChartsScreen(
    navController: NavController,
    viewModel: ChartsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isExtendedChartsVisible by viewModel.isExtendedChartsVisible.collectAsStateWithLifecycle()
    val canUseExtendedCharts by viewModel.canUseExtendedCharts.collectAsStateWithLifecycle()
    val showExtendedChartsUpsell by viewModel.showExtendedChartsUpsell.collectAsStateWithLifecycle()
    val isPollenVisible by viewModel.isPollenVisible.collectAsStateWithLifecycle()
    val showPollenUpsell by viewModel.showPollenUpsell.collectAsStateWithLifecycle()
    val locationGranted by viewModel.locationGranted.collectAsStateWithLifecycle()
    val syncChartsScroll by viewModel.syncChartsScroll.collectAsStateWithLifecycle()
    val sharedScrollState = rememberVicoScrollState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshPermissions()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isTablet = configuration.screenWidthDp >= 600

        if (isTablet) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Levý sloupec: voliče a statistiky
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    when {
                        isExtendedChartsVisible && canUseExtendedCharts -> {
                            TimeRangeSelector(
                                selected = uiState.selectedRange,
                                canUseExtendedCharts = true,
                                showBadge = false,
                                onSelected = viewModel::onRangeSelected
                            )
                        }
                        showExtendedChartsUpsell -> {
                            PremiumUpsellCard(
                                feature = PremiumFeature.EXTENDED_CHARTS,
                                onUpgradeClick = { context.startActivity(Intent(context, PremiumActivity::class.java)) }
                            )
                        }
                    }

                    SummaryRow(
                        uiState = uiState,
                        onStatClick = { SymptomDetailActivity.start(context, uiState.selectedRange) }
                    )

                    if (uiState.chartData != null) {
                        SymptomFilterRow(
                            selected = uiState.selectedSymptomFilter,
                            onSelected = viewModel::onFilterSelected
                        )
                    }
                }

                // Pravý sloupec: adaptivní mřížka s grafy
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                ) {
                    uiState.chartData?.let { data ->
                        val days = uiState.selectedRange.days

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 340.dp),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (!locationGranted) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    PermissionRequiredCard(
                                        title = stringResource(Res.string.permission_required_location_title),
                                        description = stringResource(Res.string.permission_required_location_body),
                                        actionLabel = stringResource(Res.string.permission_required_go_to_settings),
                                        onAction = { navController.navigate(Screen.Settings.route) }
                                    )
                                }
                            } else {
                                item {
                                    PremiumGate(
                                        isVisible = isPollenVisible,
                                        showUpsell = showPollenUpsell,
                                        feature = PremiumFeature.POLLEN_DATA,
                                        onUpgradeClick = { context.startActivity(Intent(context, PremiumActivity::class.java)) }
                                    ) {
                                        ChartCard(title = stringResource(Res.string.chart_pollen_vs_symptoms), icon = Icons.Outlined.FilterVintage) {
                                            PollenVsSymptomChart(data = data, days = days, scrollState = if (syncChartsScroll) sharedScrollState else null)
                                        }
                                    }
                                }

                                item {
                                    ChartCard(title = stringResource(Res.string.chart_aqi_vs_symptoms), icon = Icons.Outlined.Air) {
                                        AqiVsSymptomChart(data = data, days = days, scrollState = if (syncChartsScroll) sharedScrollState else null)
                                    }
                                }
                            }

                            item {
                                ChartCard(title = stringResource(Res.string.chart_symptom_intensity), icon = Icons.AutoMirrored.Outlined.ShowChart) {
                                    SymptomIntensityChart(data = data, days = days, scrollState = if (syncChartsScroll) sharedScrollState else null)
                                }
                            }
                        }
                    } ?: run {
                        EmptyChartsCard()
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                when {
                    isExtendedChartsVisible && canUseExtendedCharts -> item {
                        TimeRangeSelector(
                            selected = uiState.selectedRange,
                            canUseExtendedCharts = true,
                            showBadge = false,
                            onSelected = viewModel::onRangeSelected
                        )
                    }
                    showExtendedChartsUpsell -> item {
                        PremiumUpsellCard(
                            feature = PremiumFeature.EXTENDED_CHARTS,
                            onUpgradeClick = { context.startActivity(Intent(context, PremiumActivity::class.java)) }
                        )
                    }
                }

                item {
                    SummaryRow(
                        uiState = uiState,
                        onStatClick = { SymptomDetailActivity.start(context, uiState.selectedRange) }
                    )
                }

                uiState.chartData?.let { data ->
                    val days = uiState.selectedRange.days

                    item {
                        SymptomFilterRow(
                            selected = uiState.selectedSymptomFilter,
                            onSelected = viewModel::onFilterSelected
                        )
                    }

                    if (!locationGranted) {
                        item {
                            PermissionRequiredCard(
                                title = stringResource(Res.string.permission_required_location_title),
                                description = stringResource(Res.string.permission_required_location_body),
                                actionLabel = stringResource(Res.string.permission_required_go_to_settings),
                                onAction = { navController.navigate(Screen.Settings.route) }
                            )
                        }
                    } else {
                        item {
                            PremiumGate(
                                isVisible = isPollenVisible,
                                showUpsell = showPollenUpsell,
                                feature = PremiumFeature.POLLEN_DATA,
                                onUpgradeClick = { context.startActivity(Intent(context, PremiumActivity::class.java)) }
                            ) {
                                ChartCard(title = stringResource(Res.string.chart_pollen_vs_symptoms), icon = Icons.Outlined.FilterVintage) {
                                    PollenVsSymptomChart(data = data, days = days, scrollState = if (syncChartsScroll) sharedScrollState else null)
                                }
                            }
                        }
                    }
                    if (locationGranted) {
                        item {
                            ChartCard(title = stringResource(Res.string.chart_aqi_vs_symptoms), icon = Icons.Outlined.Air) {
                                AqiVsSymptomChart(data = data, days = days, scrollState = if (syncChartsScroll) sharedScrollState else null)
                            }
                        }
                    }
                    item {
                        ChartCard(title = stringResource(Res.string.chart_symptom_intensity), icon = Icons.AutoMirrored.Outlined.ShowChart) {
                            SymptomIntensityChart(data = data, days = days, scrollState = if (syncChartsScroll) sharedScrollState else null)
                        }
                    }
                } ?: run {
                    item { EmptyChartsCard() }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
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
fun SummaryRow(uiState: ChartsUiState, onStatClick: () -> Unit) {
    val mostFrequentLabel = uiState.mostFrequentSymptomType
        ?.let { stringResource(it.labelRes()) }
        ?: "—"

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CardTitle(
            text = stringResource(Res.string.stats_symptom_summary),
            icon = Icons.Outlined.BarChart
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(modifier = Modifier.weight(1f), value = uiState.totalSymptomEvents.toString(), label = stringResource(Res.string.stat_total), onClick = onStatClick)
            StatCard(modifier = Modifier.weight(1f), value = "%.1f".format(uiState.avgDailyIntensity), label = stringResource(Res.string.stat_avg), onClick = onStatClick)
            StatCard(modifier = Modifier.weight(1f), value = mostFrequentLabel, label = stringResource(Res.string.stat_most_frequent), onClick = onStatClick)
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, value: String, label: String, onClick: (() -> Unit)? = null) {
    AppCard(modifier = modifier, onClick = onClick) {
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
            stringResource(Res.string.filter_symptoms_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = MonoFontFamily
        )
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selected == null,
                    onClick = { onSelected(null) },
                    label = { Text(stringResource(Res.string.filter_all), fontFamily = MonoFontFamily, fontSize = 10.sp) },
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
                    leadingIcon = {
                    Icon(imageVector = type.icon(), contentDescription = null, modifier = Modifier.size(14.dp))
                },
                label = { Text(label.uppercase(), fontFamily = MonoFontFamily, fontSize = 10.sp) },
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
private fun rememberXAxisFormatter(dateLabels: List<String>): CartesianValueFormatter {
    return remember(dateLabels) {
        CartesianValueFormatter { _, value, _ ->
            dateLabels.getOrElse(value.toInt()) { "" }.ifBlank { "—" }
        }
    }
}

@Composable
private fun rememberXAxisItemPlacer(days: Int): HorizontalAxis.ItemPlacer {
    val step = when {
        days <= 7 -> 1
        days <= 14 -> 2
        else -> 5
    }
    return remember(step) {
        HorizontalAxis.ItemPlacer.aligned(spacing = { step })
    }
}

@Composable
fun SymptomIntensityChart(data: ChartDataSet, days: Int, scrollState: VicoScrollState? = null) {
    val symptomColor = MaterialTheme.colorScheme.symptomColor
    val modelProducer = remember { CartesianChartModelProducer() }
    val xFormatter = rememberXAxisFormatter(data.dateLabels)
    val xItemPlacer = rememberXAxisItemPlacer(days)
    val ownScrollState = rememberVicoScrollState()
    val hasData = data.symptomPoints.isNotEmpty() && data.symptomPoints.any { it.avgIntensity > 0f }

    LaunchedEffect(data) {
        if (hasData) {
            modelProducer.runTransaction {
                columnSeries {
                    series(x = data.symptomPoints.map { it.dayIndex }, y = data.symptomPoints.map { it.avgIntensity })
                }
            }
        }
    }

    if (hasData) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = ColumnProvider.series(rememberLineComponent(fill = Fill(symptomColor), thickness = 12.dp))
                ),
                startAxis = VerticalAxis.rememberStart(
                    valueFormatter = CartesianValueFormatter { _, value, _ -> "%.0f".format(value) },
                    itemPlacer = VerticalAxis.ItemPlacer.step(step = { 1.0 })
                ),
                bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = xFormatter, itemPlacer = xItemPlacer)
            ),
            modelProducer = modelProducer,
            scrollState = scrollState ?: ownScrollState,
            modifier = Modifier.fillMaxWidth().height(180.dp)
        )
    } else {
        EmptyChartPlaceholder(stringResource(Res.string.chart_empty_period))
    }
}

@Composable
fun PollenVsSymptomChart(data: ChartDataSet, days: Int, scrollState: VicoScrollState? = null) {
    val pollenColor = MaterialTheme.colorScheme.secondary
    val symptomColor = MaterialTheme.colorScheme.symptomColor
    val modelProducer = remember { CartesianChartModelProducer() }
    val xFormatter = rememberXAxisFormatter(data.dateLabels)
    val xItemPlacer = rememberXAxisItemPlacer(days)
    val ownScrollState = rememberVicoScrollState()
    val hasPollenData = data.environmentPoints.any { it.pollenAvailable }
    val pollenPoints = data.environmentPoints.filter { it.pollenAvailable }

    LaunchedEffect(data) {
        if (hasPollenData) {
            modelProducer.runTransaction {
                lineSeries {
                    series(x = pollenPoints.map { it.dayIndex }, y = pollenPoints.map { it.pollenGrass })
                    series(x = data.symptomPoints.map { it.dayIndex }, y = data.symptomPoints.map { it.avgIntensity })
                }
            }
        }
    }

    if (hasPollenData) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.Line(fill = LineFill.single(Fill(pollenColor))),
                        LineCartesianLayer.Line(fill = LineFill.single(Fill(symptomColor)))
                    )
                ),
                startAxis = VerticalAxis.rememberStart(
                    valueFormatter = CartesianValueFormatter { _, value, _ -> "%.0f".format(value) },
                    itemPlacer = VerticalAxis.ItemPlacer.step(step = { 1.0 })
                ),
                bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = xFormatter, itemPlacer = xItemPlacer)
            ),
            modelProducer = modelProducer,
            scrollState = scrollState ?: ownScrollState,
            modifier = Modifier.fillMaxWidth().height(180.dp)
        )
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(color = pollenColor, label = stringResource(Res.string.legend_pollen_grass))
            LegendItem(color = symptomColor, label = stringResource(Res.string.legend_symptoms))
        }
    } else {
        AppCard(borderColor = MaterialTheme.colorScheme.outline) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Column {
                    Text(stringResource(Res.string.pollen_unavailable_title), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    Text(stringResource(Res.string.pollen_unavailable_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun AqiVsSymptomChart(data: ChartDataSet, days: Int, scrollState: VicoScrollState? = null) {
    val aqiColor = MaterialTheme.colorScheme.okColor
    val symptomColor = MaterialTheme.colorScheme.symptomColor
    val modelProducer = remember { CartesianChartModelProducer() }
    val xFormatter = rememberXAxisFormatter(data.dateLabels)
    val xItemPlacer = rememberXAxisItemPlacer(days)
    val ownScrollState = rememberVicoScrollState()
    val hasData = data.environmentPoints.any { it.aqi > 0f } || data.symptomPoints.any { it.avgIntensity > 0f }

    LaunchedEffect(data) {
        if (hasData) {
            modelProducer.runTransaction {
                lineSeries {
                    series(x = data.environmentPoints.map { it.dayIndex }, y = data.environmentPoints.map { it.aqi })
                    series(x = data.symptomPoints.map { it.dayIndex }, y = data.symptomPoints.map { it.avgIntensity })
                }
            }
        }
    }

    if (hasData) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.Line(fill = LineFill.single(Fill(aqiColor))),
                        LineCartesianLayer.Line(fill = LineFill.single(Fill(symptomColor)))
                    )
                ),
                startAxis = VerticalAxis.rememberStart(
                    valueFormatter = CartesianValueFormatter { _, value, _ -> "%.0f".format(value) },
                    itemPlacer = VerticalAxis.ItemPlacer.step(step = { 1.0 })
                ),
                bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = xFormatter, itemPlacer = xItemPlacer)
            ),
            modelProducer = modelProducer,
            scrollState = scrollState ?: ownScrollState,
            modifier = Modifier.fillMaxWidth().height(180.dp)
        )
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(color = aqiColor, label = stringResource(Res.string.legend_aqi))
            LegendItem(color = symptomColor, label = stringResource(Res.string.legend_symptoms))
        }
    } else {
        EmptyChartPlaceholder(stringResource(Res.string.chart_empty_period))
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
            Icon(
                imageVector = Icons.Outlined.BarChart,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(stringResource(Res.string.empty_charts_title), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                stringResource(Res.string.empty_charts_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
