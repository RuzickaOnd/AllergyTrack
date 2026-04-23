package com.allergytrack.app.ui.screens.analysis

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.allergytrack.app.domain.model.PremiumFeature
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.domain.model.Correlation
import com.allergytrack.app.domain.model.CorrelationSeverity
import com.allergytrack.app.domain.model.TriggerType
import com.allergytrack.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
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

    // Export success snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.exportSuccess) {
        if (uiState.exportSuccess) {
            snackbarHostState.showSnackbar("Report exported successfully")
            viewModel.dismissExportSuccess()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        if (!isAnalysisVisible) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("This feature is currently unavailable.", style = MaterialTheme.typography.bodyMedium)
            }
            return@Scaffold
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentGreen)
            }
            return@Scaffold
        }

        // Detail panel — shown if a correlation is selected
        uiState.selectedCorrelation?.let { corr ->
            CorrelationDetailSheet(
                correlation = corr,
                onDismiss = { viewModel.onCorrelationSelected(null) }
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(24.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeading(
                        title = "AI Analysis",
                        subtitle = "Correlation and trigger insights"
                    )
                    
                    if (isExportVisible && uiState.hasEnoughData) {
                        IconButton(
                            onClick = viewModel::exportReport,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SurfaceVariant)
                                .border(1.dp, Border, CircleShape)
                        ) {
                            if (uiState.isExporting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = AccentGreen)
                            } else {
                                Icon(Icons.Default.Share, "Export report", tint = TextPrimary)
                            }
                        }
                    }
                }
            }

            if (!uiState.hasEnoughData) {
                item {
                    AnalysisLockedCard(
                        symptomCount = uiState.symptomCount,
                        daysUntilAnalysis = uiState.daysUntilAnalysis
                    )
                }
            } else if (showAnalysisUpsell) {
                item {
                    PremiumUpsellCard(feature = PremiumFeature.AI_CORRELATION_ANALYSIS)
                }
            } else {
                // AI Summary
                item {
                    AiBadge(text = uiState.aiSummary)
                }

                // Rankings title
                item {
                    CardTitle(text = "Top Triggers")
                }

                // Correlation list
                if (uiState.correlations.isEmpty()) {
                    item {
                        AppCard {
                            Text(
                                "No significant patterns found yet. Keep logging to improve analysis.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
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

            // Export button
            if (uiState.hasEnoughData) {
                item {
                    PremiumGate(
                        isVisible = isExportVisible,
                        showUpsell = showExportUpsell,
                        feature = PremiumFeature.PDF_EXPORT
                    ) {
                        SecondaryButton(
                            text = "Export PDF Report",
                            onClick = viewModel::exportReport
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun CorrelationItem(
    correlation: Correlation,
    onClick: () -> Unit
) {
    val scorePercent = (correlation.score * 100).toInt()
    val severityColor = when (correlation.severity) {
        CorrelationSeverity.HIGH -> DotRed
        CorrelationSeverity.MEDIUM -> DotYellow
        CorrelationSeverity.LOW -> AccentGreen
    }
    val triggerIcon = when (correlation.triggerType) {
        TriggerType.FOOD -> "🍽"
        TriggerType.ENVIRONMENT -> "🌿"
        TriggerType.COMBINED -> "⚡"
    }

    AppCard(
        modifier = Modifier.clickable(onClick = onClick),
        borderColor = severityColor.copy(alpha = 0.3f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Score indicator
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
                Text(
                    "$triggerIcon ${correlation.trigger}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    "${correlation.symptomType.label} • ${correlation.occurrences} matches",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextDim,
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
        CorrelationSeverity.HIGH -> DotRed
        CorrelationSeverity.MEDIUM -> DotYellow
        CorrelationSeverity.LOW -> AccentGreen
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        // Custom header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
            }
            Text(
                correlation.trigger,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                modifier = Modifier.padding(start = 8.dp),
                fontFamily = SerifFontFamily
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // Big score card
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
                            "CORRELATION CONFIDENCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Stats grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailStatCard(
                        modifier = Modifier.weight(1f),
                        label = "MATCHES",
                        value = "${correlation.occurrences}/${correlation.totalEvents}"
                    )
                    DetailStatCard(
                        modifier = Modifier.weight(1f),
                        label = "LATENCY",
                        value = "${correlation.avgDelayHours.toInt()}h"
                    )
                    DetailStatCard(
                        modifier = Modifier.weight(1f),
                        label = "IMPACT",
                        value = correlation.symptomType.emoji
                    )
                }
            }

            // Interpretation
            item {
                CardTitle(text = "Clinical Insight")
                AiBadge(text = generateInterpretation(correlation))
            }

            item {
                AppCard {
                    Text(
                        text = "Recommendation",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = getRecommendation(correlation),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun DetailStatCard(modifier: Modifier = Modifier, label: String, value: String) {
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = TextDim,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun AnalysisLockedCard(symptomCount: Int, daysUntilAnalysis: Int) {
    val progress = (symptomCount.toFloat() / 5f).coerceIn(0f, 1f)

    AppCard(borderColor = Border) {
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
                "Analysis Warming Up",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Our AI engine needs more data to identify patterns accurately. Keep logging your symptoms and triggers.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            // Progress bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "DATA MATURITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextDim
                    )
                    Text(
                        "$symptomCount / 5 LOGS",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = AccentGreen,
                    trackColor = Border
                )
            }
        }
    }
}

private fun getRecommendation(correlation: Correlation): String {
    return when (correlation.triggerType) {
        TriggerType.FOOD -> "Consider a 14-day elimination trial for ${correlation.trigger} while maintaining your symptom log to verify this correlation."
        TriggerType.ENVIRONMENT -> "Try to minimize outdoor exposure when ${correlation.trigger} levels are high, or use an air purifier indoors."
        TriggerType.COMBINED -> "This complex trigger suggests multiple factors. Consult with an allergist and share this report for a professional assessment."
    }
}

private fun generateInterpretation(correlation: Correlation): String {
    val scorePercent = (correlation.score * 100).toInt()
    return when (correlation.triggerType) {
        TriggerType.FOOD ->
            "${correlation.trigger} is identified as a strong potential trigger for ${correlation.symptomType.label} with $scorePercent% confidence. Symptoms typically manifest ~${correlation.avgDelayHours.toInt()}h after consumption."
        TriggerType.ENVIRONMENT ->
            "High levels of ${correlation.trigger} strongly correlate with your ${correlation.symptomType.label} episodes. We've detected this pattern in $scorePercent% of observed days."
        TriggerType.COMBINED ->
            "Synergistic effect detected: The combination of ${correlation.trigger} and recent environmental factors significantly increases reaction intensity."
    }
}
