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
            Text("Tato funkce je momentálně nedostupná.", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentGreen)
        }
        return
    }

    // Detail panel — shown if a correlation is selected
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
            .padding(horizontal = 20.dp),
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
                CardTitle(text = "Hlavní spouštěče")
            }

            // Correlation list
            if (uiState.correlations.isEmpty()) {
                item {
                    AppCard {
                        Text(
                            "Zatím nebyly nalezeny žádné výrazné vzory. Pokračuj v logování.",
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
                        text = "Exportovat PDF report",
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

    // Poznámka: Tato obrazovka je nyní volána v kontextu jednoho Scaffold v MainAppScaffold
    // ale protože nahrazuje celý obsah, postaráme se o layout sami.
    
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
                        "SPOLEHLIVOST KORELACE",
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
                    label = "SHODY",
                    value = "${correlation.occurrences}/${correlation.totalEvents}"
                )
                DetailStatCard(
                    modifier = Modifier.weight(1f),
                    label = "LATENCE",
                    value = "${correlation.avgDelayHours.toInt()}h"
                )
                DetailStatCard(
                    modifier = Modifier.weight(1f),
                    label = "DOPAD",
                    value = correlation.symptomType.emoji
                )
            }
        }

        // Interpretation
        item {
            CardTitle(text = "Klinický vhled")
            AiBadge(text = generateInterpretation(correlation))
        }

        item {
            AppCard {
                Text(
                    text = "Doporučení",
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

        item {
            SecondaryButton(text = "ZAVŘÍT", onClick = onDismiss)
        }

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
                "Analýza se připravuje",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Náš AI model potřebuje více dat pro přesnou identifikaci vzorců. Pokračuj v logování příznaků a jídla.",
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
                        "ZRÁLOST DAT",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextDim
                    )
                    Text(
                        "$symptomCount / 5 ZÁZNAMŮ",
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
        TriggerType.FOOD -> "Zvaž 14denní eliminační test pro ${correlation.trigger} a sleduj, zda se příznaky zlepší."
        TriggerType.ENVIRONMENT -> "Snaž se minimalizovat pobyt venku, když je hladina ${correlation.trigger} vysoká, nebo používej čističku vzduchu."
        TriggerType.COMBINED -> "Tato kombinace naznačuje více faktorů. Konzultuj tento report se svým alergologem."
    }
}

private fun generateInterpretation(correlation: Correlation): String {
    val scorePercent = (correlation.score * 100).toInt()
    return when (correlation.triggerType) {
        TriggerType.FOOD ->
            "${correlation.trigger} byl identifikován jako silný spouštěč pro ${correlation.symptomType.label} s $scorePercent% jistotou. Příznaky se typicky objevují ~${correlation.avgDelayHours.toInt()}h po konzumaci."
        TriggerType.ENVIRONMENT ->
            "Vysoké hladiny ${correlation.trigger} silně korelují s tvými epizodami ${correlation.symptomType.label}. Tento vzorec jsme detekovali v $scorePercent% sledovaných dnů."
        TriggerType.COMBINED ->
            "Detekován synergický efekt: Kombinace ${correlation.trigger} a nedávných faktorů prostředí výrazně zvyšuje intenzitu reakce."
    }
}
