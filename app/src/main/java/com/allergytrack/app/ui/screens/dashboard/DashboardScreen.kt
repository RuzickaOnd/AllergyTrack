package com.allergytrack.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.allergytrack.app.ui.util.labelRes
import com.allergytrack.app.data.local.database.entity.SymptomLogEntity
import com.allergytrack.app.domain.model.EnvironmentLog
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.navigation.Screen
import com.allergytrack.app.ui.theme.*
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

            item {
                AiBadge(text = "Druhý den vysokého pylu travin — sleduj své příznaky.")
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun EnvironmentCard(env: EnvironmentLog) {
    AppCard(borderColor = MaterialTheme.colorScheme.warningColor.copy(alpha = 0.3f)) {
        CardTitle(stringResource(R.string.env_section_title))
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            EnvironmentStat(
                label = stringResource(R.string.env_label_pollen_grass),
                value = env.pollenGrassIndex?.toString() ?: "—",
                color = if ((env.pollenGrassIndex ?: 0) >= 3) MaterialTheme.colorScheme.warningColor else MaterialTheme.colorScheme.okColor
            )
            EnvironmentStat(
                label = stringResource(R.string.env_label_aqi),
                value = env.aqi.toString(),
                color = if (env.aqi >= 50) MaterialTheme.colorScheme.warningColor else MaterialTheme.colorScheme.okColor
            )
            EnvironmentStat(
                label = stringResource(R.string.env_label_pollen_tree),
                value = env.pollenTreeIndex?.toString() ?: "—",
                color = if ((env.pollenTreeIndex ?: 0) >= 3) MaterialTheme.colorScheme.warningColor else MaterialTheme.colorScheme.okColor
            )
        }
    }
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(MaterialTheme.colorScheme.okColor, androidx.compose.foundation.shape.CircleShape)
        )
        Spacer(Modifier.width(16.dp))
        Column {
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
    }
}

@Composable
fun TimelineSymptomItem(log: SymptomLogEntity) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val symptomType = try { com.allergytrack.app.domain.model.SymptomType.valueOf(log.type) } catch (_: IllegalArgumentException) { null }
    val typeLabel = symptomType?.let { stringResource(it.labelRes()) } ?: log.type

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(MaterialTheme.colorScheme.symptomColor, androidx.compose.foundation.shape.CircleShape)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = timeFormat.format(Date(log.timestamp)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.symptomColor.copy(alpha = 0.7f),
                fontFamily = MonoFontFamily
            )
            Text(
                text = "$typeLabel (${log.intensity}/5)",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.symptomColor,
                fontWeight = FontWeight.Bold
            )
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
