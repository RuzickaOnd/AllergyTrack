package com.allergytrack.app.ui.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import android.widget.Toast
import com.allergytrack.app.R
import com.allergytrack.app.domain.model.AnalysisReadiness
import com.allergytrack.app.domain.model.Correlation
import com.allergytrack.app.domain.model.CorrelationSeverity
import com.allergytrack.app.domain.model.FoodTrigger
import com.allergytrack.app.domain.model.PremiumFeature
import com.allergytrack.app.domain.model.TriggerType
import com.allergytrack.app.ui.components.AiBadge
import com.allergytrack.app.ui.components.AppCard
import com.allergytrack.app.ui.components.CardTitle
import com.allergytrack.app.ui.components.PremiumGate
import com.allergytrack.app.ui.components.PremiumUpsellCard
import com.allergytrack.app.ui.components.SecondaryButton
import com.allergytrack.app.ui.theme.okColor
import com.allergytrack.app.ui.theme.symptomColor
import com.allergytrack.app.ui.theme.warningColor
import com.allergytrack.app.ui.util.AnalysisExportHandler
import com.allergytrack.app.ui.util.foodTriggerIcon
import com.allergytrack.app.ui.util.icon
import com.allergytrack.app.ui.util.labelRes

@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isAnalysisVisible by viewModel.isAnalysisVisible.collectAsStateWithLifecycle()
    val showAnalysisUpsell by viewModel.showAnalysisUpsell.collectAsStateWithLifecycle()
    val isExportVisible by viewModel.isExportVisible.collectAsStateWithLifecycle()
    val showExportUpsell by viewModel.showExportUpsell.collectAsStateWithLifecycle()

    AnalysisExportHandler(viewModel = viewModel)

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

    // Zobrazení podle stavu připravenosti
    when (val readiness = uiState.readiness) {
        is AnalysisReadiness.Locked -> {
            AnalysisLockedScreen(readiness = readiness)
            return
        }

        is AnalysisReadiness.Partial, is AnalysisReadiness.Full -> {
            // Pokračovat k zobrazení analýzy
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (uiState.showDisclaimer) {
            item {
                AnalysisDisclaimerBanner(disclaimer = uiState.disclaimer)
            }
        }

        if (showAnalysisUpsell) {
            item { PremiumUpsellCard(feature = PremiumFeature.AI_CORRELATION_ANALYSIS) }
        } else {
            item { AiBadge(text = uiState.aiSummary) }

            item { CardTitle(text = stringResource(R.string.analysis_top_triggers), icon = Icons.Outlined.Insights) }

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
                        onClick = {
                            CorrelationDetailActivity.start(
                                context = context,
                                trigger = correlation.trigger,
                                triggerType = correlation.triggerType.name,
                                symptomType = correlation.symptomType.name
                            )
                        }
                    )
                }
            }
        }

        // ── Sekce: Spouštěče jídla ──────────────────────────────────────
        if (uiState.foodTriggers.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Spouštěče z jídla",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Jídla a alergeny nejčastěji spojené s reakcemi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }

            items(
                items = uiState.foodTriggers.take(6),  // max 6 karet
                key = { it.allergenKey }
            ) { trigger ->
                FoodTriggerCard(
                    trigger = trigger,
                    onClick = {
                        FoodTriggerDetailActivity.start(context, trigger.allergenKey)
                    }
                )
                Spacer(Modifier.height(8.dp))
            }

            // Tlačítko "zobrazit vše" pokud je více než 6
            if (uiState.foodTriggers.size > 6) {
                item {
                    TextButton(
                        onClick = { /* TODO: rozbalit / zobrazit všechny */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Zobrazit všechny spouštěče (${uiState.foodTriggers.size})")
                    }
                }
            }

        } else if (uiState.foodTriggersLoading) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                }
            }
        } else if (!uiState.foodTriggersLoading && uiState.readiness !is AnalysisReadiness.Locked) {
            item {
                // Empty state — dostatek dat pro analýzu, ale žádné korelace nenalezeny
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        "Zatím nejsou nalezeny žádné vzorce. " +
                                "Zaznamenávej jídlo a příznaky pravidelně — analýza se zlepšuje s časem.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

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
}

@Composable
fun CorrelationItem(
    correlation: Correlation,
    onClick: () -> Unit
) {
    val scorePercent = (correlation.score * 100).toInt()
    val severityColor = when (correlation.severity) {
        CorrelationSeverity.HIGH -> MaterialTheme.colorScheme.symptomColor
        CorrelationSeverity.MEDIUM -> MaterialTheme.colorScheme.warningColor
        CorrelationSeverity.LOW -> MaterialTheme.colorScheme.okColor
    }
    val triggerIcon = correlation.triggerType.icon()
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
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = triggerIcon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            correlation.trigger,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
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
fun DetailStatCard(modifier: Modifier = Modifier, label: String, value: String = "", icon: ImageVector? = null) {
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurface)
            } else {
                Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
        }
    }
}

@Composable
fun AnalysisLockedScreen(readiness: AnalysisReadiness.Locked) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Zatím nedostatek dat",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            readiness.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))

        // Progress bar — počet symptomů, ne dny
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Zaznamenané symptomy",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    "${readiness.symptomCount} / ${readiness.requiredCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { readiness.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Každý symptom se počítá — " +
                        "zaznamenej jak se cítíš kdykoli během dne.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AnalysisDisclaimerBanner(disclaimer: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                disclaimer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )
        }
    }
}

@Composable
fun FoodTriggerCard(
    trigger: FoodTrigger,
    onClick: () -> Unit
) {
    val severityColor = when (trigger.severity) {
        CorrelationSeverity.HIGH -> MaterialTheme.colorScheme.symptomColor
        CorrelationSeverity.MEDIUM -> MaterialTheme.colorScheme.warningColor
        CorrelationSeverity.LOW -> MaterialTheme.colorScheme.okColor
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(10.dp),
                color = severityColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = foodTriggerIcon(trigger.allergenKey),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = severityColor
                    )
                }
            }

            // Název + symptomy
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        trigger.triggerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (trigger.isExpected) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "SHODA",
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (trigger.dominantSymptoms.isNotEmpty()) {
                    Text(
                        trigger.dominantSymptoms.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                // Progress bar
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { trigger.correlationScore },
                    modifier = Modifier.fillMaxWidth(),
                    color = severityColor,
                    trackColor = severityColor.copy(alpha = 0.12f)
                )
            }

            // Skóre
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${(trigger.correlationScore * 100).toInt()} %",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = severityColor
                )
                Text(
                    "${trigger.reactionCount}/${trigger.totalCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
