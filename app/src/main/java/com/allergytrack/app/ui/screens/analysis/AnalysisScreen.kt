package com.allergytrack.app.ui.screens.analysis

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.ManageSearch
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Lightbulb
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
import androidx.compose.ui.draw.scale
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
import com.allergytrack.app.data.remote.food.FoodAllergenHelper
import com.allergytrack.app.domain.model.AnalysisReadiness
import com.allergytrack.app.domain.model.Correlation
import com.allergytrack.app.domain.model.CorrelationSeverity
import com.allergytrack.app.domain.model.FoodTrigger
import com.allergytrack.app.domain.model.FoodReactionEvent
import com.allergytrack.app.domain.model.PremiumFeature
import com.allergytrack.app.domain.model.SymptomType
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
import com.allergytrack.app.ui.theme.MonoFontFamily
import com.allergytrack.app.ui.theme.SerifFontFamily
import com.allergytrack.app.ui.util.AnalysisExportHandler
import com.allergytrack.app.ui.util.foodTriggerIcon
import com.allergytrack.app.ui.util.icon
import com.allergytrack.app.ui.util.labelRes
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

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

    // HIDDEN mode → ComingSoonScreen; VISIBLE+FREE → glimpse & lock (pokračuj dál)
    if (!isAnalysisVisible && !showAnalysisUpsell) {
        AnalysisComingSoonScreen()
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

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var selectedCorrelation by remember { mutableStateOf<Correlation?>(null) }
    var selectedFoodTrigger by remember { mutableStateOf<FoodTrigger?>(null) }
    var selectedReactionEvent by remember { mutableStateOf<FoodReactionEvent?>(null) }

    LaunchedEffect(uiState.correlations, uiState.foodTriggers) {
        if (isTablet && !showAnalysisUpsell && selectedCorrelation == null && selectedFoodTrigger == null) {
            uiState.correlations.firstOrNull()?.let {
                selectedCorrelation = it
                selectedFoodTrigger = null
                selectedReactionEvent = null
            } ?: uiState.foodTriggers.firstOrNull()?.let {
                selectedFoodTrigger = it
                selectedCorrelation = null
                selectedReactionEvent = null
            }
        }
    }

    if (isTablet) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Levý sloupec (Master): AI interpret, top korelace, spouštěče jídla
            LazyColumn(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.showDisclaimer) {
                    item {
                        AnalysisDisclaimerBanner(disclaimer = uiState.disclaimer)
                    }
                }

                if (showAnalysisUpsell) {
                    // ── GLIMPSE & LOCK (VISIBLE + FREE) ─────────────────────────
                    if (uiState.aiSummary.isNotEmpty()) {
                        item { AiBadgeGlimpse(text = uiState.aiSummary) }
                    }
                    if (uiState.foodTriggers.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.analysis_food_triggers_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                stringResource(R.string.analysis_food_triggers_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        item {
                            FoodTriggerCard(trigger = uiState.foodTriggers.first(), onClick = {})
                            Spacer(Modifier.height(8.dp))
                        }
                        val lockedTriggers = uiState.foodTriggers.drop(1).take(3)
                        items(lockedTriggers, key = { "locked_${it.allergenKey}" }) { trigger ->
                            LockedFoodTriggerCard(trigger = trigger)
                            Spacer(Modifier.height(8.dp))
                        }
                        val hiddenCount = uiState.foodTriggers.size - 1 - lockedTriggers.size
                        if (hiddenCount > 0) {
                            item {
                                Text(
                                    stringResource(R.string.analysis_free_more_triggers, hiddenCount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                    item { PremiumUpsellCard(feature = PremiumFeature.AI_CORRELATION_ANALYSIS) }
                } else {
                    // ── PLNÁ ANALÝZA (PREMIUM) ───────────────────────────────────
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
                            val isSelected = selectedCorrelation?.trigger == correlation.trigger && selectedCorrelation?.symptomType == correlation.symptomType
                            val highlightColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(highlightColor)
                            ) {
                                CorrelationItem(
                                    correlation = correlation,
                                    onClick = {
                                        selectedCorrelation = correlation
                                        selectedFoodTrigger = null
                                        selectedReactionEvent = null
                                    }
                                )
                            }
                        }
                    }

                    // ── Sekce: Spouštěče jídla ──────────────────────────────────
                    if (uiState.foodTriggers.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.analysis_food_triggers_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                stringResource(R.string.analysis_food_triggers_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        items(
                            items = uiState.foodTriggers.take(6),
                            key = { it.allergenKey }
                        ) { trigger ->
                            val isSelected = selectedFoodTrigger?.allergenKey == trigger.allergenKey
                            val highlightColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(highlightColor)
                            ) {
                                FoodTriggerCard(
                                    trigger = trigger,
                                    onClick = {
                                        selectedFoodTrigger = trigger
                                        selectedCorrelation = null
                                        selectedReactionEvent = null
                                    }
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        if (uiState.foodTriggers.size > 6) {
                            item {
                                TextButton(
                                    onClick = { /* TODO: rozbalit / zobrazit všechny */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.analysis_show_all_triggers, uiState.foodTriggers.size))
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
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    stringResource(R.string.analysis_no_food_patterns),
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

            // Pravý sloupec (Detail): Detailní statistiky vybraného spouštěče
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showAnalysisUpsell) {
                    Spacer(Modifier.height(8.dp))
                    PremiumUpsellCard(feature = PremiumFeature.AI_CORRELATION_ANALYSIS)
                } else if (selectedCorrelation != null) {
                    val corr = selectedCorrelation!!
                    val scorePercent = (corr.score * 100).toInt()
                    val severityColor = when (corr.severity) {
                        CorrelationSeverity.HIGH   -> MaterialTheme.colorScheme.symptomColor
                        CorrelationSeverity.MEDIUM -> MaterialTheme.colorScheme.warningColor
                        CorrelationSeverity.LOW    -> MaterialTheme.colorScheme.okColor
                    }
                    val localizedName = localizedTriggerName(corr)

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = localizedName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SerifFontFamily
                    )

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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailStatCard(modifier = Modifier.weight(1f), label = stringResource(R.string.stat_matches), value = "${corr.occurrences}/${corr.totalEvents}")
                        DetailStatCard(modifier = Modifier.weight(1f), label = stringResource(R.string.stat_latency), value = "${corr.avgDelayHours.toInt()}h")
                        DetailStatCard(modifier = Modifier.weight(1f), label = stringResource(R.string.stat_impact), icon = corr.symptomType.icon())
                    }

                    CardTitle(text = stringResource(R.string.correlation_insight_title), icon = Icons.Outlined.Lightbulb)
                    AiBadge(text = generateInterpretation(corr))

                    AppCard {
                        Text(
                            text = stringResource(R.string.correlation_recommendation_title),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = getRecommendation(corr),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else if (selectedFoodTrigger != null) {
                    val trigger = selectedFoodTrigger!!
                    val localizedName = if (trigger.allergenKey.startsWith("__food__")) {
                        trigger.triggerName
                    } else {
                        FoodAllergenHelper.keyToDisplayRes[trigger.allergenKey]
                            ?.let { stringResource(it) }
                            ?: trigger.triggerName
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = localizedName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SerifFontFamily
                    )

                    FoodTriggerSummaryCard(trigger = trigger)

                    FoodReactionFrequencyCard(trigger = trigger)

                    if (trigger.dominantSymptoms.isNotEmpty()) {
                        FoodSymptomMatrixCard(trigger = trigger)
                    }

                    Text(
                        stringResource(R.string.food_trigger_history_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        stringResource(R.string.food_trigger_history_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    trigger.reactionEvents.forEach { event ->
                        FoodEventCard(
                            event = event,
                            isExpanded = selectedReactionEvent?.foodLogId == event.foodLogId,
                            onClick = {
                                selectedReactionEvent = if (selectedReactionEvent?.foodLogId == event.foodLogId) null else event
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Zvolte spouštěč pro zobrazení detailů",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    } else {
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
                // ── GLIMPSE & LOCK (VISIBLE + FREE) ─────────────────────────
                if (uiState.aiSummary.isNotEmpty()) {
                    item { AiBadgeGlimpse(text = uiState.aiSummary) }
                }
                if (uiState.foodTriggers.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.analysis_food_triggers_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            stringResource(R.string.analysis_food_triggers_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    item {
                        FoodTriggerCard(trigger = uiState.foodTriggers.first(), onClick = {})
                    }
                    val lockedTriggers = uiState.foodTriggers.drop(1).take(3)
                    items(lockedTriggers, key = { "locked_${it.allergenKey}" }) { trigger ->
                        Spacer(Modifier.height(8.dp))
                        LockedFoodTriggerCard(trigger = trigger)
                    }
                    val hiddenCount = uiState.foodTriggers.size - 1 - lockedTriggers.size
                    if (hiddenCount > 0) {
                        item {
                            Text(
                                stringResource(R.string.analysis_free_more_triggers, hiddenCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
                item { PremiumUpsellCard(feature = PremiumFeature.AI_CORRELATION_ANALYSIS) }
            } else {
                // ── PLNÁ ANALÝZA (PREMIUM) ───────────────────────────────────
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

                // ── Sekce: Spouštěče jídla ──────────────────────────────────
                if (uiState.foodTriggers.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.analysis_food_triggers_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            stringResource(R.string.analysis_food_triggers_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    items(
                        items = uiState.foodTriggers.take(6),
                        key = { it.allergenKey }
                    ) { trigger ->
                        FoodTriggerCard(
                            trigger = trigger,
                            onClick = { FoodTriggerDetailActivity.start(context, trigger.allergenKey) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    if (uiState.foodTriggers.size > 6) {
                        item {
                            TextButton(
                                onClick = { /* TODO: rozbalit / zobrazit všechny */ },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.analysis_show_all_triggers, uiState.foodTriggers.size))
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
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                stringResource(R.string.analysis_no_food_patterns),
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
    val localizedTrigger = if (correlation.triggerType == TriggerType.FOOD) {
        FoodAllergenHelper.keyToDisplayRes[correlation.trigger]
            ?.let { stringResource(it) }
            ?: correlation.trigger
    } else {
        correlation.trigger
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
                            localizedTrigger,
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
            stringResource(R.string.analysis_locked_no_data),
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
                    stringResource(R.string.analysis_symptom_count_label),
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
                stringResource(R.string.analysis_log_encouragement),
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
fun AnalysisComingSoonScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "coming_soon")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.26f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .scale(iconScale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Text(
            stringResource(R.string.analysis_coming_soon_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(10.dp))

        Text(
            stringResource(R.string.analysis_coming_soon_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(32.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ComingSoonFeatureRow(
                icon = Icons.AutoMirrored.Outlined.ShowChart,
                text = stringResource(R.string.analysis_coming_feature_1)
            )
            ComingSoonFeatureRow(
                icon = Icons.AutoMirrored.Outlined.ManageSearch,
                text = stringResource(R.string.analysis_coming_feature_2)
            )
            ComingSoonFeatureRow(
                icon = Icons.AutoMirrored.Outlined.Article,
                text = stringResource(R.string.analysis_coming_feature_3)
            )
        }
    }
}

@Composable
private fun ComingSoonFeatureRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AiBadgeGlimpse(text: String) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        AiBadge(text = text)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.25f to Color.Transparent,
                            1.0f to surfaceColor
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = stringResource(R.string.analysis_free_ai_locked),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontFamily = MonoFontFamily
            )
        }
    }
}

@Composable
private fun LockedFoodTriggerCard(trigger: FoodTrigger) {
    val localizedName = if (trigger.allergenKey.startsWith("__food__")) {
        trigger.triggerName
    } else {
        FoodAllergenHelper.keyToDisplayRes[trigger.allergenKey]
            ?.let { stringResource(it) }
            ?: trigger.triggerName
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = foodTriggerIcon(trigger.allergenKey),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            Text(
                text = localizedName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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
                val localizedTriggerName = if (trigger.allergenKey.startsWith("__food__")) {
                    trigger.triggerName
                } else {
                    FoodAllergenHelper.keyToDisplayRes[trigger.allergenKey]
                        ?.let { stringResource(it) }
                        ?: trigger.triggerName
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        localizedTriggerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (trigger.isExpected) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                stringResource(R.string.correlation_badge_expected),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (trigger.dominantSymptoms.isNotEmpty()) {
                    val localizedSymptoms = trigger.dominantSymptoms.map { typeName ->
                        val type = try { SymptomType.valueOf(typeName) } catch (e: IllegalArgumentException) { null }
                        if (type != null) stringResource(type.labelRes()) else typeName
                    }
                    Text(
                        localizedSymptoms.joinToString(", "),
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
