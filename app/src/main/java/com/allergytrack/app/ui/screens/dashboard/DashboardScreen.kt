package com.allergytrack.app.ui.screens.dashboard

import com.allergytrack.shared.presentation.*
import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.allergytrack.shared.resources.*
import com.allergytrack.shared.data.local.database.entity.FoodLogEntity
import com.allergytrack.shared.data.local.database.entity.SymptomLogEntity
import com.allergytrack.shared.domain.model.EnvironmentLog
import com.allergytrack.shared.domain.model.SymptomType
import com.allergytrack.app.ui.components.AiBadge
import com.allergytrack.app.ui.components.AppCard
import com.allergytrack.app.ui.components.CardTitle
import com.allergytrack.app.ui.components.PermissionRequiredCard
import com.allergytrack.app.ui.components.PrimaryButton
import com.allergytrack.app.ui.navigation.Screen
import com.allergytrack.app.ui.screens.profile.PremiumActivity
import com.allergytrack.app.ui.theme.MonoFontFamily
import com.allergytrack.app.ui.theme.SerifFontFamily
import com.allergytrack.app.ui.theme.okColor
import com.allergytrack.app.ui.theme.symptomColor
import com.allergytrack.app.ui.theme.warningColor
import com.allergytrack.app.ui.util.icon
import com.allergytrack.app.ui.util.labelRes
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showNotificationBanner by viewModel.showNotificationBanner.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isAnalysisHidden by viewModel.isAnalysisHidden.collectAsStateWithLifecycle()
    val triggerCount by viewModel.potentialTriggerCount.collectAsStateWithLifecycle()

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
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isTablet = configuration.screenWidthDp >= 600

        if (isTablet) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Levý sloupec: tlačítka zápisu + karta ovzduší + doporučení AI
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PrimaryButton(
                            text = stringResource(Res.string.btn_add_food),
                            onClick = { navController.navigate(Screen.FoodLog.route) },
                            modifier = Modifier.weight(1f)
                        )
                        PrimaryButton(
                            text = stringResource(Res.string.btn_add_symptom),
                            onClick = { navController.navigate(Screen.SymptomLog.route) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (showNotificationBanner) {
                        NotificationReminderBanner(
                            onDismiss = { viewModel.dismissNotificationBanner() },
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                        )
                    }

                    if (uiState.showPremiumPromoCard) {
                        PremiumPromoCard(
                            onClick = { context.startActivity(Intent(context, PremiumActivity::class.java)) }
                        )
                    }

                    if (isAnalysisHidden && triggerCount > 0) {
                        AnalysisTeaserCard(
                            count = triggerCount,
                            onClick = {
                                viewModel.logAiAnalysisCardClicked()
                                navController.navigate(Screen.Analysis.route)
                            }
                        )
                    }

                    if (uiState.showEnvironmentSection) {
                        CardTitle(text = stringResource(Res.string.env_section_title), icon = Icons.Outlined.Air)
                        when {
                            !uiState.locationGranted -> PermissionRequiredCard(
                                title = stringResource(Res.string.permission_required_location_title),
                                description = stringResource(Res.string.permission_required_location_body),
                                actionLabel = stringResource(Res.string.permission_required_go_to_settings),
                                onAction = { navController.navigate(Screen.Settings.route) }
                            )

                            uiState.latestEnvironment != null -> uiState.latestEnvironment?.let {
                                EnvironmentCard(
                                    env = it,
                                    showPollenPremiumBadge = uiState.showPollenPremiumBadge
                                )
                            } ?: run { NoEnvironmentCard() }

                            else -> NoEnvironmentCard()
                        }
                    }

                    InsightBadge(uiState.insight)
                }

                // Pravý sloupec: Dnešní záznamy (timeline)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CardTitle(text = stringResource(Res.string.section_today_records), icon = Icons.Outlined.CalendarToday)

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
                        EmptyDayCard()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                AppCard {
                                    allItems.forEachIndexed { index, (type, item) ->
                                        when (type) {
                                            "food" if item is FoodLogEntity ->
                                                TimelineFoodItem(item)

                                            "symptom" if item is SymptomLogEntity ->
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
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PrimaryButton(
                            text = stringResource(Res.string.btn_add_food),
                            onClick = { navController.navigate(Screen.FoodLog.route) },
                            modifier = Modifier.weight(1f)
                        )
                        PrimaryButton(
                            text = stringResource(Res.string.btn_add_symptom),
                            onClick = { navController.navigate(Screen.SymptomLog.route) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (showNotificationBanner) {
                    item {
                        NotificationReminderBanner(
                            onDismiss = { viewModel.dismissNotificationBanner() },
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                        )
                    }
                }

                if (uiState.showPremiumPromoCard) {
                    item {
                        PremiumPromoCard(
                            onClick = { context.startActivity(Intent(context, PremiumActivity::class.java)) }
                        )
                    }
                }

                if (isAnalysisHidden && triggerCount > 0) {
                    item {
                        AnalysisTeaserCard(
                            count = triggerCount,
                            onClick = {
                                viewModel.logAiAnalysisCardClicked()
                                navController.navigate(Screen.Analysis.route)
                            }
                        )
                    }
                }

                if (uiState.showEnvironmentSection) {
                    item {
                        when {
                            !uiState.locationGranted -> PermissionRequiredCard(
                                title = stringResource(Res.string.permission_required_location_title),
                                description = stringResource(Res.string.permission_required_location_body),
                                actionLabel = stringResource(Res.string.permission_required_go_to_settings),
                                onAction = { navController.navigate(Screen.Settings.route) }
                            )

                            uiState.latestEnvironment != null -> uiState.latestEnvironment?.let {
                                EnvironmentCard(
                                    env = it,
                                    showPollenPremiumBadge = uiState.showPollenPremiumBadge
                                )
                            } ?: run { NoEnvironmentCard() }

                            else -> NoEnvironmentCard()
                        }
                    }
                }

                item {
                    CardTitle(text = stringResource(Res.string.section_today_records), icon = Icons.Outlined.CalendarToday)
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
                                when (type) {
                                    "food" if item is FoodLogEntity ->
                                        TimelineFoodItem(item)

                                    "symptom" if item is SymptomLogEntity ->
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
            }
        }
    }
}

@Composable
fun NotificationReminderBanner(
    onDismiss: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bell")
    val bellRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3200
                0f at 0 using LinearEasing
                -14f at 150 using LinearEasing
                14f at 300 using LinearEasing
                -10f at 450 using LinearEasing
                10f at 600 using LinearEasing
                0f at 750 using LinearEasing
                0f at 3200 using LinearEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "bellRotation"
    )

    AppCard(
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        onClick = onNavigateToSettings
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsActive,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(bellRotation),
                    tint = MaterialTheme.colorScheme.primary
                )

                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.dashboard_notification_banner_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Res.string.dashboard_notification_banner_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(Res.string.dashboard_notification_banner_dismiss),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PremiumPromoCard(onClick: () -> Unit) {
    AppCard(
        onClick = onClick,
        borderColor = MaterialTheme.colorScheme.warningColor.copy(alpha = 0.45f),
        backgroundColor = MaterialTheme.colorScheme.warningColor.copy(alpha = 0.08f),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.warningColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.warningColor
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(Res.string.dashboard_premium_promo_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(Res.string.dashboard_premium_promo_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(Res.string.dashboard_premium_promo_cta).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = MonoFontFamily,
                color = MaterialTheme.colorScheme.warningColor
            )
        }
    }
}

@Composable
fun InsightBadge(insight: DashboardInsight?) {
    if (insight == null) return
    val message = when (insight) {
        is DashboardInsight.HighPollen ->
            stringResource(Res.string.dashboard_insight_high_pollen, insight.maxLevel)

        is DashboardInsight.HighAqi ->
            stringResource(Res.string.dashboard_insight_high_aqi, insight.aqi)

        DashboardInsight.GoodDay ->
            stringResource(Res.string.dashboard_insight_good_day)
    }
    AiBadge(text = message)
}

@Composable
fun EnvironmentCard(
    env: EnvironmentLog,
    showPollenPremiumBadge: Boolean = false
) {
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
        CardTitle(stringResource(Res.string.env_section_title), icon = Icons.Outlined.Air)
        Spacer(Modifier.height(12.dp))
        if (env.pollenAvailable || showPollenPremiumBadge) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                EnvironmentStat(
                    label = stringResource(Res.string.env_label_pollen_grass),
                    value = env.pollenGrassIndex?.toString() ?: "—",
                    color = pollenStatColor(env.pollenGrassIndex),
                    isLocked = showPollenPremiumBadge
                )
                EnvironmentStat(
                    label = stringResource(Res.string.env_label_aqi),
                    value = env.aqi.toString(),
                    color = aqiStatColor(env.aqi)
                )
                EnvironmentStat(
                    label = stringResource(Res.string.env_label_pollen_tree),
                    value = env.pollenTreeIndex?.toString() ?: "—",
                    color = pollenStatColor(env.pollenTreeIndex),
                    isLocked = showPollenPremiumBadge
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                EnvironmentStat(
                    label = stringResource(Res.string.env_label_aqi),
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
fun EnvironmentStat(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    isLocked: Boolean = false
) {
    val contentAlpha = if (isLocked) 0.45f else 1f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        val statContent: @Composable () -> Unit = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = color.copy(alpha = contentAlpha),
                    fontFamily = SerifFontFamily
                )
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                    fontFamily = MonoFontFamily
                )
            }
        }

        if (isLocked) {
            Box(
                modifier = Modifier.padding(end = 6.dp, bottom = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .blur(12.dp)
                ) {
                    statContent()
                }
                PremiumPollenBadge(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 6.dp, y = 6.dp)
                )
            }
        } else {
            statContent()
        }
    }
}

@Composable
private fun PremiumPollenBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(22.dp),
        color = MaterialTheme.colorScheme.warningColor.copy(alpha = 0.16f),
        shape = CircleShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.warningColor
            )
        }
    }
}

@Composable
fun TimelineFoodItem(log: FoodLogEntity) {
    val timeFormat = SimpleDateFormat("HH:mm", LocalLocale.current.platformLocale)
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
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .size(14.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun TimelineSymptomItem(log: SymptomLogEntity) {
    val timeFormat = SimpleDateFormat("HH:mm", LocalLocale.current.platformLocale)
    val symptomType = try {
        SymptomType.valueOf(log.type)
    } catch (_: IllegalArgumentException) {
        null
    }
    val typeLabel = symptomType?.let { stringResource(it.labelRes()) } ?: log.type
    val symptomIcon = symptomType?.icon() ?: Icons.Outlined.Air

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = symptomIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(Res.string.empty_day_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontFamily = SerifFontFamily,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                stringResource(Res.string.empty_day_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun AnalysisTeaserCard(count: Int, onClick: () -> Unit) {
    AppCard(
        onClick = onClick,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(Res.string.dashboard_analysis_teaser_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(Res.string.dashboard_analysis_teaser_body, count),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
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
                stringResource(Res.string.loading_environment),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = MonoFontFamily
            )
        }
    }
}
