package com.allergytrack.app.ui.screens.analysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allergytrack.shared.resources.*
import com.allergytrack.shared.data.remote.food.FoodAllergenHelper
import com.allergytrack.shared.domain.model.Correlation
import com.allergytrack.shared.domain.model.CorrelationSeverity
import com.allergytrack.shared.domain.model.TriggerType
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.theme.*
import com.allergytrack.app.ui.util.icon
import com.allergytrack.app.ui.util.labelRes

@Composable
fun CorrelationDetailScreenRoot(
    onBack: () -> Unit,
    viewModel: CorrelationDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val correlation = uiState.correlation
        if (correlation != null) {
            CorrelationDetailScreen(
                correlation = correlation,
                onBack = onBack
            )
        } else {
            LaunchedEffect(Unit) { onBack() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorrelationDetailScreen(
    correlation: Correlation,
    onBack: () -> Unit
) {
    val scorePercent = (correlation.score * 100).toInt()
    val severityColor = when (correlation.severity) {
        CorrelationSeverity.HIGH   -> MaterialTheme.colorScheme.symptomColor
        CorrelationSeverity.MEDIUM -> MaterialTheme.colorScheme.warningColor
        CorrelationSeverity.LOW    -> MaterialTheme.colorScheme.okColor
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.analysis_correlation_detail)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.cd_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            item {
                AppCard(borderColor = severityColor.copy(alpha = 0.4f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "$scorePercent%",
                            style = MaterialTheme.typography.headlineLarge,
                            color = severityColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(Res.string.correlation_reliability),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailStatCard(modifier = Modifier.weight(1f), label = stringResource(Res.string.stat_matches), value = "${correlation.occurrences}/${correlation.totalEvents}")
                    DetailStatCard(modifier = Modifier.weight(1f), label = stringResource(Res.string.stat_latency), value = "${correlation.avgDelayHours.toInt()}h")
                    DetailStatCard(modifier = Modifier.weight(1f), label = stringResource(Res.string.stat_impact), icon = correlation.symptomType.icon())
                }
            }

            item {
                CardTitle(text = stringResource(Res.string.correlation_insight_title), icon = Icons.Outlined.Lightbulb)
                AiBadge(text = generateInterpretation(correlation))
            }

            item {
                AppCard {
                    Text(
                        text = stringResource(Res.string.correlation_recommendation_title),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = getRecommendation(correlation),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun localizedTriggerName(correlation: Correlation): String =
    if (correlation.triggerType == TriggerType.FOOD) {
        FoodAllergenHelper.keyToDisplayRes[correlation.trigger]
            ?.let { stringResource(it) }
            ?: correlation.trigger
    } else {
        correlation.trigger
    }

@Composable
fun getRecommendation(correlation: Correlation): String {
    val trigger = localizedTriggerName(correlation)
    return when (correlation.triggerType) {
        TriggerType.FOOD        -> stringResource(Res.string.recommendation_food, trigger)
        TriggerType.ENVIRONMENT -> stringResource(Res.string.recommendation_environment, trigger)
        TriggerType.COMBINED    -> stringResource(Res.string.recommendation_combined)
    }
}

@Composable
fun generateInterpretation(correlation: Correlation): String {
    val scorePercent = (correlation.score * 100).toInt()
    val symptomLabel = stringResource(correlation.symptomType.labelRes())
    val trigger = localizedTriggerName(correlation)
    return when (correlation.triggerType) {
        TriggerType.FOOD ->
            stringResource(Res.string.interpretation_food, trigger, symptomLabel, scorePercent, correlation.avgDelayHours.toInt())
        TriggerType.ENVIRONMENT ->
            stringResource(Res.string.interpretation_environment, trigger, symptomLabel, scorePercent)
        TriggerType.COMBINED ->
            stringResource(Res.string.interpretation_combined, trigger)
    }
}
