package com.allergytrack.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.allergytrack.app.R
import com.allergytrack.app.data.local.database.entity.FoodLogEntity
import com.allergytrack.app.data.local.database.entity.SymptomLogEntity
import com.allergytrack.app.domain.model.EnvironmentLog
import com.allergytrack.app.domain.model.SymptomType
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.navigation.Screen
import com.allergytrack.app.ui.theme.*
import com.allergytrack.app.ui.util.labelRes
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refreshEnvironment()
        }
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(
                    text = stringResource(uiState.greetingRes),
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = SerifFontFamily,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Normal
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrimaryButton(
                        text = stringResource(R.string.btn_add_food),
                        onClick = { navController.navigate(Screen.FoodLog.route) },
                        modifier = Modifier.weight(1f)
                    )
                    SecondaryButton(
                        text = stringResource(R.string.btn_add_symptom),
                        onClick = { navController.navigate(Screen.SymptomLog.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (uiState.showEnvironmentSection) {
                item {
                    uiState.latestEnvironment?.let { env ->
                        EnvironmentCard(env = env)
                    } ?: NoEnvironmentCard()
                }
            }

            item {
                CardTitle(text = stringResource(R.string.section_today_records))
            }

            val allItems = buildList {
                uiState.todayFoodLogs.forEach { add(Pair("food", it)) }
                uiState.todaySymptomLogs.forEach { add(Pair("symptom", it)) }
            }.sortedByDescending {
                when (val item = it.second) {
                    is FoodLogEntity -> item.timestamp
                    is SymptomLogEntity -> item.timestamp
                    else -> 0L
                }
            }

            if (allItems.isEmpty()) {
                item { EmptyDayCard() }
            } else {
                item {
                    AppCard {
                        allItems.forEachIndexed { index, (type, item) ->
                            when {
                                type == "food" && item is FoodLogEntity ->
                                    TimelineFoodItem(item)
                                type == "symptom" && item is SymptomLogEntity ->
                                    TimelineSymptomItem(item)
                            }
                            if (index < allItems.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }

            item { InsightBadge(uiState.insight) }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun InsightBadge(insight: DashboardInsight?) {
    if (insight == null) return
    val message = when (insight) {
        is DashboardInsight.HighPollen ->
            stringResource(R.string.dashboard_insight_high_pollen, insight.maxLevel)
        is DashboardInsight.HighAqi ->
            stringResource(R.string.dashboard_insight_high_aqi, insight.aqi)
        DashboardInsight.GoodDay ->
            stringResource(R.string.dashboard_insight_good_day)
    }
    AiBadge(text = message)
}

@Composable
fun EnvironmentCard(env: EnvironmentLog) {
    val maxPollen = if (env.pollenAvailable) {
        maxOf(env.pollenGrassIndex ?: 0, env.pollenTreeIndex ?: 0, env.pollenWeedIndex ?: 0)
    } else null

    val borderColor = when {
        maxPollen != null && maxPollen >= 4 -> MaterialTheme.colorScheme.symptomColor.copy(alpha = 0.4f)
        maxPollen != null && maxPollen >= 2 -> MaterialTheme.colorScheme.warningColor.copy(alpha = 0.4f)
        env.aqi >= 100 -> MaterialTheme.colorScheme.symptomColor.copy(alpha = 0.3f)
        env.aqi >= 50 -> MaterialTheme.colorScheme.warningColor.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    }

    AppCard(borderColor = borderColor) {
        CardTitle(stringResource(R.string.env_section_title))
        Spacer(Modifier.height(12.dp))
        if (env.pollenAvailable) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                EnvironmentStat(
                    label = stringResource(R.string.env_label_pollen_grass),
                    value = env.pollenGrassIndex?.toString() ?: "—",
                    color = pollenStatColor(env.pollenGrassIndex)
                )
                EnvironmentStat(
                    label = stringResource(R.string.env_label_aqi),
                    value = env.aqi.toString(),
                    color = aqiStatColor(env.aqi)
                )
                EnvironmentStat(
                    label = stringResource(R.string.env_label_pollen_tree),
                    value = env.pollenTreeIndex?.toString() ?: "—",
                    color = pollenStatColor(env.pollenTreeIndex)
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                EnvironmentStat(
                    label = stringResource(R.string.env_label_aqi),
                    value = env.aqi.toString(),
                    color = aqiStatColor(env.aqi)
                )
            }
        }
    }
}

@Composable
private fun pollenStatColor(index: Int?) = when {
    index == null -> MaterialTheme.colorScheme.onSurfaceVariant
    index >= 4 -> MaterialTheme.colorScheme.symptomColor
    index >= 2 -> MaterialTheme.colorScheme.warningColor
    else -> MaterialTheme.colorScheme.okColor
}

@Composable
private fun aqiStatColor(aqi: Int) = when {
    aqi >= 100 -> MaterialTheme.colorScheme.symptomColor
    aqi >= 50 -> MaterialTheme.colorScheme.warningColor
    else -> MaterialTheme.colorScheme.okColor
}

@Composable
fun EnvironmentStat(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            fontFamily = SerifFontFamily
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = MonoFontFamily
        )
    }
}

@Composable
fun TimelineFoodItem(log: FoodLogEntity) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val hasAllergens = log.allergens.length > 2  // more than "[]"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(MaterialTheme.colorScheme.okColor, CircleShape)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = timeFormat.format(Date(log.timestamp)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = MonoFontFamily
            )
            Text(
                text = log.foodName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
        if (hasAllergens) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            ) {
                Text(
                    text = "⚠️",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun TimelineSymptomItem(log: SymptomLogEntity) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val symptomType = try { SymptomType.valueOf(log.type) } catch (_: IllegalArgumentException) { null }
    val typeLabel = symptomType?.let { stringResource(it.labelRes()) } ?: log.type
    val emoji = symptomType?.emoji ?: "⚡"

    val intensityColor = when {
        log.intensity >= 4 -> MaterialTheme.colorScheme.symptomColor
        log.intensity >= 3 -> MaterialTheme.colorScheme.warningColor
        else -> MaterialTheme.colorScheme.okColor
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(intensityColor, CircleShape)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = timeFormat.format(Date(log.timestamp)),
                style = MaterialTheme.typography.labelMedium,
                color = intensityColor.copy(alpha = 0.7f),
                fontFamily = MonoFontFamily
            )
            Text(
                text = "$emoji $typeLabel",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(5) { i ->
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            if (i < log.intensity) intensityColor
                            else MaterialTheme.colorScheme.outline
                        )
                )
            }
        }
    }
}

@Composable
fun EmptyDayCard() {
    AppCard {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.empty_day_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontFamily = SerifFontFamily
            )
            Text(
                stringResource(R.string.empty_day_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun NoEnvironmentCard() {
    AppCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.width(12.dp))
            Text(
                stringResource(R.string.loading_environment),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = MonoFontFamily
            )
        }
    }
}
