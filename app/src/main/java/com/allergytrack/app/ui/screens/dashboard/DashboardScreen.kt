package com.allergytrack.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
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

            // Stálá tlačítka pro logování — vždy viditelná
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrimaryButton(
                        text = "+ JÍDLO",
                        onClick = { navController.navigate(Screen.FoodLog.route) },
                        modifier = Modifier.weight(1f)
                    )
                    SecondaryButton(
                        text = "+ PŘÍZNAK",
                        onClick = { navController.navigate(Screen.SymptomLog.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Environment card — zobrazíme pouze pokud jsme v Evropě (nebo ještě načítáme)
            if (uiState.showEnvironmentSection) {
                item {
                    uiState.latestEnvironment?.let { env ->
                        EnvironmentCard(env = env)
                    } ?: NoEnvironmentCard()
                }
            }

            // Today's records header
            item {
                CardTitle(text = "📋 DNEŠNÍ ZÁZNAMY")
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

            // AI Tip
            item {
                AiBadge(text = "Druhý den vysokého pylu travin — sleduj své příznaky.")
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun EnvironmentCard(env: EnvironmentLog) {
    AppCard(borderColor = AccentOrange.copy(alpha = 0.3f)) {
        CardTitle("🌿 PROSTŘEDÍ DNES")
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            EnvironmentStat(
                label = "Pyl trav",
                value = env.pollenGrassIndex?.toString() ?: "—",
                color = if ((env.pollenGrassIndex ?: 0) >= 3) AccentOrange else AccentGreen
            )
            EnvironmentStat(
                label = "AQI",
                value = env.aqi.toString(),
                color = if (env.aqi >= 50) AccentOrange else AccentGreen
            )
            EnvironmentStat(
                label = "Pyl stromů",
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
fun EmptyDayCard() {
    AppCard {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Dnes zatím žádné záznamy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontFamily = SerifFontFamily
            )
            Text(
                "Zaloguj snídani nebo jak se cítíš.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
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
                color = AccentBlue
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Načítám data prostředí...",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim,
                fontFamily = MonoFontFamily
            )
        }
    }
}
