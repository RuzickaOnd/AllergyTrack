package com.allergytrack.app.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.allergytrack.shared.resources.*
import org.jetbrains.compose.resources.StringResource
import com.allergytrack.app.ui.components.AppCard

private data class FaqEntry(val questionRes: StringResource, val answerRes: StringResource)

@Composable
fun FaqScreen(navController: NavController) {
    val items = remember {
        listOf(
            FaqEntry(Res.string.faq_q_aqi, Res.string.faq_a_aqi),
            FaqEntry(Res.string.faq_q_pollen_scale, Res.string.faq_a_pollen_scale),
            FaqEntry(Res.string.faq_q_analysis_unlock, Res.string.faq_a_analysis_unlock),
            FaqEntry(Res.string.faq_q_how_correlations, Res.string.faq_a_how_correlations),
            FaqEntry(Res.string.faq_q_correlation_percent, Res.string.faq_a_correlation_percent),
            FaqEntry(Res.string.faq_q_match_badge, Res.string.faq_a_match_badge),
            FaqEntry(Res.string.faq_q_food_database, Res.string.faq_a_food_database),
            FaqEntry(Res.string.faq_q_no_pollen, Res.string.faq_a_no_pollen),
            FaqEntry(Res.string.faq_q_eu_allergens, Res.string.faq_a_eu_allergens),
            FaqEntry(Res.string.faq_q_data_safety, Res.string.faq_a_data_safety),
        )
    }

    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        items.forEachIndexed { index, item ->
            FaqItem(
                question = stringResource(item.questionRes),
                answer = stringResource(item.answerRes),
                isExpanded = expandedIndex == index,
                onToggle = {
                    expandedIndex = if (expandedIndex == index) null else index
                }
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FaqItem(
    question: String,
    answer: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrow_rotation"
    )

    AppCard(contentPadding = PaddingValues(0.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(arrowRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        thickness = 0.5.dp
                    )
                    Text(
                        text = answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                }
            }
        }
    }
}
