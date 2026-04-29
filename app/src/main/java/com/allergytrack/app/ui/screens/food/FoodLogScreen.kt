package com.allergytrack.app.ui.screens.food

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.allergytrack.app.R
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.navigation.Screen
import com.allergytrack.app.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodLogScreen(
    navController: NavController,
    viewModel: FoodLogViewModel = hiltViewModel()
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        item {
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                onClick = { navController.navigate(Screen.Camera.route) }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.fillMaxSize().padding(8.dp)) {
                        val cornerSize = 24.dp
                        val thickness = 3.dp
                        Box(Modifier.size(cornerSize).border(thickness, MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 8.dp)))
                        Box(Modifier.align(Alignment.TopEnd).size(cornerSize).border(thickness, MaterialTheme.colorScheme.primary, RoundedCornerShape(topEnd = 8.dp)))
                        Box(Modifier.align(Alignment.BottomStart).size(cornerSize).border(thickness, MaterialTheme.colorScheme.primary, RoundedCornerShape(bottomStart = 8.dp)))
                        Box(Modifier.align(Alignment.BottomEnd).size(cornerSize).border(thickness, MaterialTheme.colorScheme.primary, RoundedCornerShape(bottomEnd = 8.dp)))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📷", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.btn_scan_tap),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.btn_scan_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(
                    text = stringResource(R.string.btn_barcode),
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                )
                SecondaryButton(
                    text = stringResource(R.string.btn_voice_input),
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            AppCard(
                borderColor = MaterialTheme.colorScheme.outline,
                onClick = { navController.navigate(Screen.FoodManualSearch.route) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("🔍", fontSize = 18.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.search_food_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CardTitle(stringResource(R.string.section_recently_logged), icon = Icons.Outlined.History)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Pill(text = "Ovesné vločky")
                    Pill(text = "Řecký jogurt")
                    Pill(text = "Banán")
                    Pill(
                        text = "MLÉKO ⚠️",
                        color = MaterialTheme.colorScheme.symptomColor.copy(alpha = 0.1f),
                        textColor = MaterialTheme.colorScheme.symptomColor
                    )
                    Pill(text = "Kuřecí prsa")
                    Pill(
                        text = "Mandle ⚠️",
                        color = MaterialTheme.colorScheme.symptomColor.copy(alpha = 0.1f),
                        textColor = MaterialTheme.colorScheme.symptomColor
                    )
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}
