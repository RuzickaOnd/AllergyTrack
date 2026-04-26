package com.allergytrack.app.ui.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.allergytrack.app.R
import com.allergytrack.app.domain.model.Correlation
import com.allergytrack.app.domain.model.CorrelationSeverity
import com.allergytrack.app.domain.model.PremiumFeature
import com.allergytrack.app.domain.model.TriggerType
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.theme.*
import com.allergytrack.app.ui.util.labelRes

@Composable
fun AnalysisScreen(
    navController: NavController,
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isAnalysisVisible by viewModel.isAnalysisVisible.collectAsStateWithLifecycle()
    val showAnalysisUpsell by viewModel.showAnalysisUpsell.collectAsStateWithLifecycle()
    val isExportVisible by viewModel.isExportVisible.collectAsStateWithLifecycle()
    val showExportUpsell by viewModel.showExportUpsell.collectAsStateWithLifecycle()

    if (!isAnalysisVisible) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.feature_unavailable), style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    uiState.selectedCorrelation?.let { corr ->
        CorrelationDetailSheet(
            correlation = corr,
            onDismiss = { viewModel.onCorrelationSelected(null) }
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        if (!uiState.hasEnoughData) {
            item {
                AnalysisLockedCard(
                    symptomCount = uiState.symptomCount,
                    daysUntilAnalysis = uiState.daysUntilAnalysis
                )
            }
        } else if (showAnalysisUpsell) {
            item { PremiumUpsellCard(feature = PremiumFeature.AI_CORRELATION_ANALYSIS) }
        } else {
            item { AiBadge(text = uiState.aiSummary) }

            item { CardTitle(text = stringResource(R.string.analysis_top_triggers)) }

            if (uiState.correlations.isEmpty()) {
                item {
                    AppCard {
                        Text(
                            stringResource(R.string.analysis_no_patterns),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(uiState.correlations) { correlation ->
                    CorrelationItem(
                        correlation = correlation,
                        onClick = { viewModel.onCorrelationSelected(correlation) }
                    )
                }
            }
        }

        if (uiState.hasEnoughData) {
            item {
                PremiumGate(
                    isVisible = isExportVisible,
                    showUpsell = showExportUpsell,
                    feature = PremiumFeature.PDF_EXPORT
                ) {
                    SecondaryButton(
                        text = stringResource(R.string.btn_export_pdf),
                        onClick = viewModel::exportReport
                    )
                }
            }
        }

        item { Spacer(Modifier.height(100.dp)) }
    }
}

@Composable
fun CorrelationItem(
    correlation: Correlation,
    onClick: () -> Unit
) {
    val scorePercent = (correlation.score * 100).toInt()
    val severityColor = when (correlation.severity) {
        CorrelationSeverity.HIGH   -> MaterialTheme.colorScheme.symptomColor
        CorrelationSeverity.MEDIUM -> MaterialTheme.colorScheme.warningColor
        CorrelationSeverity.LOW    -> MaterialTheme.colorScheme.okColor
    }
    val triggerIcon = when (correlation.triggerType) {
        TriggerType.FOOD        -> "🍽"
        TriggerType.ENVIRONMENT -> "🌿"
        TriggerType.COMBINED    -> "⚡"
    }
    val symptomLabel = stringResource(correlation.symptomType.labelRes())
    val detailsText = stringResource(
        R.string.correlation_item_details,
        symptomLabel,
        correlation.occurrences,
        correlation.totalEvents
    )

    AppCard(
        modifier = Modifier.clickable(onClick = onClick),
        borderColor = severityColor.copy(alpha = 0.3f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(severityColor.copy(alpha = 0.1f))
                    .border(1.dp, severityColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$scorePercent%",
                    style = MaterialTheme.typography.labelLarge,
                    color = severityColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$triggerIcon ${correlation.trigger}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (correlation.isExpected) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.correlation_badge_expected),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    detailsText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CorrelationDetailSheet(
    correlation: Correlation,
    onDismiss: () -> Unit
) {
    val scorePercent = (correlation.score * 100).toInt()
    val severityColor = when (correlation.severity) {
        CorrelationSeverity.HIGH   -> MaterialTheme.colorScheme.symptomColor
        CorrelationSeverity.MEDIUM -> MaterialTheme.colorScheme.warningColor
        CorrelationSeverity.LOW    -> MaterialTheme.colorScheme.okColor
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
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
                        stringResource(R.string.correlation_reliability),
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
                DetailStatCard(modifier = Modifier.weight(1f), label = stringResource(R.string.stat_matches), value = "${correlation.occurrences}/${correlation.totalEvents}")
                DetailStatCard(modifier = Modifier.weight(1f), label = stringResource(R.string.stat_latency), value = "${correlation.avgDelayHours.toInt()}h")
                DetailStatCard(modifier = Modifier.weight(1f), label = stringResource(R.string.stat_impact), value = correlation.symptomType.emoji)
            }
        }

        item {
            CardTitle(text = stringResource(R.string.correlation_insight_title))
            AiBadge(text = generateInterpretation(correlation))
        }

        item {
            AppCard {
                Text(
                    text = stringResource(R.string.correlation_recommendation_title),
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

        item { SecondaryButton(text = stringResource(R.string.btn_close), onClick = onDismiss) }

        item { Spacer(Modifier.height(100.dp)) }
    }
}

@Composable
fun DetailStatCard(modifier: Modifier = Modifier, label: String, value: String) {
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
        }
    }
}

@Composable
fun AnalysisLockedCard(symptomCount: Int, daysUntilAnalysis: Int) {
    val progress = (symptomCount.toFloat() / 5f).coerceIn(0f, 1f)

    AppCard(borderColor = MaterialTheme.colorScheme.outline) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🔒",
                style = androidx.compose.ui.text.TextStyle(fontSize = 32.sp),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                stringResource(R.string.analysis_locked_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.analysis_locked_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.data_maturity), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        stringResource(R.string.data_records_progress, symptomCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.okColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.okColor,
                    trackColor = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun getRecommendation(correlation: Correlation): String =
    when (correlation.triggerType) {
        TriggerType.FOOD        -> stringResource(R.string.recommendation_food, correlation.trigger)
        TriggerType.ENVIRONMENT -> stringResource(R.string.recommendation_environment, correlation.trigger)
        TriggerType.COMBINED    -> stringResource(R.string.recommendation_combined)
    }

@Composable
private fun generateInterpretation(correlation: Correlation): String {
    val scorePercent = (correlation.score * 100).toInt()
    val symptomLabel = stringResource(correlation.symptomType.labelRes())
    return when (correlation.triggerType) {
        TriggerType.FOOD ->
            stringResource(R.string.interpretation_food, correlation.trigger, symptomLabel, scorePercent, correlation.avgDelayHours.toInt())
        TriggerType.ENVIRONMENT ->
            stringResource(R.string.interpretation_environment, correlation.trigger, symptomLabel, scorePercent)
        TriggerType.COMBINED ->
            stringResource(R.string.interpretation_combined, correlation.trigger)
    }
}
