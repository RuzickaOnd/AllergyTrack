package com.allergytrack.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.allergytrack.app.data.local.database.entity.FoodLogEntity
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

    Scaffold { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(16.dp)) }

            // Greeting header
            item {
                SectionHeading(
                    title = "${uiState.greeting} 👋",
                    subtitle = "${SimpleDateFormat("EEEE, d MMMM", Locale.US).format(Date())} · Prague"
                )
            }

            // Environment card
            item {
                uiState.latestEnvironment?.let { env ->
                    EnvironmentCard(env = env)
                } ?: NoEnvironmentCard()
            }

            // Today's records header
            item {
                CardTitle(text = "📋 TODAY'S LOGS")
            }

            // Merged and sorted timeline
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
                item {
                    EmptyDayCard(
                        onAddFood = { navController.navigate(Screen.FoodLog.route) },
                        onAddSymptom = { navController.navigate(Screen.SymptomLog.route) }
                    )
                }
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

            // AI Tip
            item {
                AiBadge(text = "2nd day of high grass pollen — monitor your symptoms")
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun EnvironmentCard(env: EnvironmentLog) {
    AppCard(borderColor = AccentOrange.copy(alpha = 0.3f)) {
        CardTitle("🌿 ENVIRONMENT TODAY")
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            EnvironmentStat(
                label = "Grass Pollen",
                value = env.pollenGrassIndex?.toString() ?: "—",
                color = if ((env.pollenGrassIndex ?: 0) >= 3) AccentOrange else AccentGreen
            )
            EnvironmentStat(
                label = "AQI",
                value = env.aqi.toString(),
                color = if (env.aqi >= 50) AccentOrange else AccentGreen
            )
            EnvironmentStat(
                label = "Tree Pollen",
                value = env.pollenTreeIndex?.toString() ?: "—",
                color = if ((env.pollenTreeIndex ?: 0) >= 3) AccentOrange else AccentGreen
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
            color = TextDim,
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
                .background(AccentGreen, androidx.compose.foundation.shape.CircleShape)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = timeFormat.format(Date(log.timestamp)),
                style = MaterialTheme.typography.labelMedium,
                color = TextDim,
                fontFamily = MonoFontFamily
            )
            Text(
                text = log.foodName,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TimelineSymptomItem(log: SymptomLogEntity) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(AccentOrange, androidx.compose.foundation.shape.CircleShape)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = timeFormat.format(Date(log.timestamp)),
                style = MaterialTheme.typography.labelMedium,
                color = AccentOrange.copy(alpha = 0.7f),
                fontFamily = MonoFontFamily
            )
            Text(
                text = "${log.type} (${log.intensity}/5)",
                style = MaterialTheme.typography.bodyLarge,
                color = AccentOrange,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyDayCard(onAddFood: () -> Unit, onAddSymptom: () -> Unit) {
    AppCard {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "No logs yet today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontFamily = SerifFontFamily
            )
            Text(
                "Start by logging your breakfast or how you're feeling right now.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(text = "+ FOOD", onClick = onAddFood, modifier = Modifier.weight(1f))
                SecondaryButton(text = "+ SYMPTOM", onClick = onAddSymptom, modifier = Modifier.weight(1f))
            }
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
                color = AccentBlue
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Fetching environmental data...",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim,
                fontFamily = MonoFontFamily
            )
        }
    }
}
