package com.allergytrack.app.ui.screens.charts

import com.allergytrack.shared.presentation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allergytrack.shared.resources.*
import com.allergytrack.app.ui.components.AppCard
import com.allergytrack.app.ui.components.CardTitle
import com.allergytrack.app.ui.theme.MonoFontFamily
import com.allergytrack.app.ui.util.icon
import com.allergytrack.app.ui.util.labelRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomDetailScreen(
    onBack: () -> Unit,
    viewModel: SymptomDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val canUseExtendedCharts by viewModel.canUseExtendedCharts.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.symptom_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.cd_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            if (canUseExtendedCharts) {
                item {
                    TimeRangeSelector(
                        selected = uiState.selectedRange,
                        canUseExtendedCharts = true,
                        showBadge = false,
                        onSelected = viewModel::onRangeSelected
                    )
                }
            }

            if (uiState.stats.isEmpty()) {
                item { SymptomDetailEmptyCard() }
            } else {
                val most = uiState.stats.first()
                val others = uiState.stats.drop(1)

                item {
                    Spacer(Modifier.height(4.dp))
                    CardTitle(
                        text = stringResource(Res.string.symptom_detail_most_frequent_heading),
                        icon = Icons.Outlined.EmojiEvents
                    )
                }

                item {
                    MostFrequentSymptomCard(stat = most)
                }

                if (others.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        CardTitle(
                            text = stringResource(Res.string.symptom_detail_other_heading),
                            icon = Icons.AutoMirrored.Outlined.FormatListBulleted
                        )
                    }
                    item {
                        OtherSymptomsCard(stats = others, maxCount = uiState.maxCount)
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun MostFrequentSymptomCard(stat: SymptomTypeStat) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer

    AppCard(
        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        borderColor = primary.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = stat.type.icon(),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = primary
                    )
                }
                Column {
                    Text(
                        text = stringResource(stat.type.labelRes()),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = onPrimaryContainer
                    )
                    Text(
                        text = pluralStringResource(Res.plurals.symptom_occurrences, stat.count, stat.count),
                        style = MaterialTheme.typography.bodyMedium,
                        color = onPrimaryContainer.copy(alpha = 0.75f)
                    )
                }
            }
            Surface(
                color = primary,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.symptom_detail_badge).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontFamily = MonoFontFamily,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.symptom_detail_avg_intensity).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = onPrimaryContainer.copy(alpha = 0.65f),
                fontFamily = MonoFontFamily
            )
            Text(
                text = "%.1f / 5".format(stat.avgIntensity),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = onPrimaryContainer
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { stat.avgIntensity / 5f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = primary,
            trackColor = primary.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun OtherSymptomsCard(stats: List<SymptomTypeStat>, maxCount: Int) {
    AppCard(contentPadding = PaddingValues(0.dp)) {
        stats.forEachIndexed { index, stat ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = stat.type.icon(),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(stat.type.labelRes()),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${stat.count} ×",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = MonoFontFamily
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { if (maxCount > 0) stat.count.toFloat() / maxCount else 0f },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "ø %.1f".format(stat.avgIntensity),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = MonoFontFamily
                    )
                }
            }
            if (index < stats.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                )
            }
        }
    }
}

@Composable
private fun SymptomDetailEmptyCard() {
    AppCard(borderColor = MaterialTheme.colorScheme.outline) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.BarChart,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(Res.string.symptom_detail_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
