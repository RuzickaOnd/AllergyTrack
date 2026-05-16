package com.allergytrack.app.ui.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.allergytrack.app.domain.model.CorrelationSeverity
import com.allergytrack.app.domain.model.FoodReactionEvent
import com.allergytrack.app.domain.model.FoodTrigger
import com.allergytrack.app.domain.model.SymptomInWindow
import com.allergytrack.app.ui.theme.symptomColor
import com.allergytrack.app.ui.theme.warningColor
import com.allergytrack.app.ui.util.foodTriggerIcon
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodTriggerDetailScreenRoot(
    onBack: () -> Unit,
    viewModel: FoodTriggerDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val trigger = uiState.trigger
        if (trigger != null) {
            FoodTriggerDetailScreen(
                trigger = trigger,
                uiState = uiState,
                onBack = onBack,
                onEventClick = viewModel::selectEvent
            )
        } else {
            // Trigger nenalezen
            LaunchedEffect(Unit) { onBack() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodTriggerDetailScreen(
    trigger: FoodTrigger,
    uiState: FoodTriggerDetailUiState,
    onBack: () -> Unit,
    onEventClick: (FoodReactionEvent?) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = foodTriggerIcon(trigger.allergenKey),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(trigger.triggerName)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zpět")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // ── Souhrnná karta ──────────────────────────────────────────
            item {
                FoodTriggerSummaryCard(trigger = trigger)
            }

            // ── Graf frekvence reakcí ────────────────────────────────────
            item {
                FoodReactionFrequencyCard(trigger = trigger)
            }

            // ── Matice symptomů ──────────────────────────────────────────
            if (trigger.dominantSymptoms.isNotEmpty()) {
                item {
                    FoodSymptomMatrixCard(trigger = trigger)
                }
            }

            // ── Reakční časová osa — seznam konzumací ────────────────────
            item {
                Text(
                    "Historie konzumací",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    "Kliknutím zobrazíš detail reakcí po konzumaci",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(
                items = trigger.reactionEvents,
                key = { it.foodLogId }
            ) { event ->
                FoodEventCard(
                    event = event,
                    isExpanded = uiState.selectedEvent?.foodLogId == event.foodLogId,
                    onClick = {
                        onEventClick(
                            if (uiState.selectedEvent?.foodLogId == event.foodLogId) null
                            else event
                        )
                    }
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ── Souhrnná karta ──────────────────────────────────────────────────────────

@Composable
fun FoodTriggerSummaryCard(trigger: FoodTrigger) {
    val severityColor = when (trigger.severity) {
        CorrelationSeverity.HIGH   -> MaterialTheme.colorScheme.symptomColor
        CorrelationSeverity.MEDIUM -> MaterialTheme.colorScheme.warningColor
        CorrelationSeverity.LOW    -> MaterialTheme.colorScheme.primary
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "${(trigger.correlationScore * 100).toInt()} %",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = severityColor
                    )
                    Text(
                        "pravděpodobnost reakce",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${trigger.reactionCount} z ${trigger.totalCount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "konzumací s reakcí",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Progress bar korelace
            LinearProgressIndicator(
                progress = { trigger.correlationScore },
                modifier = Modifier.fillMaxWidth(),
                color = severityColor,
                trackColor = severityColor.copy(alpha = 0.15f)
            )

            if (trigger.isExpected) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Odpovídá tvému alergickému profilu",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (trigger.dominantSymptoms.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Typické reakce: ${trigger.dominantSymptoms.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Graf frekvence reakcí ───────────────────────────────────────────────────

@Composable
fun FoodReactionFrequencyCard(trigger: FoodTrigger) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Frekvence reakcí",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))

            // Stacked bar: reakce vs. bez reakce
            val reactionFraction = trigger.reactionCount.toFloat() / trigger.totalCount
            val okFraction = 1f - reactionFraction

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp)),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                if (reactionFraction > 0) {
                    Box(
                        modifier = Modifier
                            .weight(reactionFraction)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.symptomColor),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (reactionFraction > 0.25f) {
                            Text(
                                "${trigger.reactionCount}× reakce",
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
                if (okFraction > 0) {
                    Box(
                        modifier = Modifier
                            .weight(okFraction)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (okFraction > 0.2f) {
                            Text(
                                "${trigger.totalCount - trigger.reactionCount}× bez reakce",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendItem(
                    color = MaterialTheme.colorScheme.symptomColor,
                    label = "S reakcí"
                )
                LegendItem(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    label = "Bez reakce",
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun LegendItem(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = textColor)
    }
}

// ── Matice symptomů ─────────────────────────────────────────────────────────

@Composable
fun FoodSymptomMatrixCard(trigger: FoodTrigger) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Nejčastější reakce po konzumaci",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))

            trigger.dominantSymptoms.forEachIndexed { index, symptomName ->
                val count = trigger.reactionEvents
                    .flatMap { it.reactions }
                    .count { it.symptomName == symptomName }
                val fraction = count.toFloat() / trigger.reactionCount.coerceAtLeast(1)

                if (index > 0) Spacer(Modifier.height(6.dp))
                Text(
                    symptomName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.symptomColor,
                        trackColor = MaterialTheme.colorScheme.symptomColor.copy(alpha = 0.12f)
                    )
                    Text(
                        "$count×",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(28.dp)
                    )
                }
            }
        }
    }
}

// ── Karta jedné konzumace ────────────────────────────────────────────────────

@Composable
fun FoodEventCard(
    event: FoodReactionEvent,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("d. M., HH:mm", Locale.forLanguageTag("cs"))
    val dateStr = dateFormat.format(Date(event.consumedAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (event.hadReaction)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (event.hadReaction) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (event.hadReaction)
                            MaterialTheme.colorScheme.symptomColor
                        else
                            MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            event.foodName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            dateStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (event.hadReaction && event.maxIntensity != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.symptomColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "intenzita ${event.maxIntensity}/5",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.symptomColor
                        )
                    }
                }
            }

            // Expandovaný detail — reakční časová osa
            if (isExpanded && event.reactions.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(10.dp))
                Text(
                    "Průběh reakcí:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                event.reactions.forEach { symptom ->
                    ReactionTimelineItem(symptom = symptom)
                    Spacer(Modifier.height(4.dp))
                }
            } else if (isExpanded && !event.hadReaction) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Po této konzumaci nebyly zaznamenány žádné symptomy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ReactionTimelineItem(symptom: SymptomInWindow) {
    val delayStr = when {
        symptom.delayHours < 1f -> "${(symptom.delayHours * 60).toInt()} min"
        symptom.delayHours < 24f -> "+${symptom.delayHours.toInt()} h"
        else -> "+${(symptom.delayHours / 24).toInt()} d"
    }
    val intensityColor = when {
        symptom.intensity >= 4 -> MaterialTheme.colorScheme.symptomColor
        symptom.intensity >= 2 -> MaterialTheme.colorScheme.warningColor
        else -> MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            delayStr,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(44.dp)
        )
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(50))
                .background(intensityColor)
        )
        Text(
            symptom.symptomName,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            "${symptom.intensity}/5",
            style = MaterialTheme.typography.labelSmall,
            color = intensityColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
