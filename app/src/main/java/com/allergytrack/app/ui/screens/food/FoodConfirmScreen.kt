package com.allergytrack.app.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.allergytrack.app.R
import com.allergytrack.app.data.remote.food.FoodAllergenHelper
import com.allergytrack.app.data.remote.food.RecognizedFood
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.navigation.Screen
import com.allergytrack.app.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodConfirmScreen(
    navController: NavController,
    foodName: String,
    allergens: String,
    viewModel: FoodLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var notes by remember { mutableStateOf("") }

    val parsedAllergens = allergens
        .split("|")
        .filter { it.isNotEmpty() }

    LaunchedEffect(uiState) {
        if (uiState is FoodLogUiState.Saved) {
            viewModel.reset()
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Dashboard.route) { inclusive = true }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        item {
            AppCard(
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text(
                            text = foodName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.food_ai_identified),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            CardTitle(text = stringResource(R.string.section_detected_allergens), icon = Icons.Outlined.Warning)
            if (parsedAllergens.isNotEmpty()) {
                AppCard(
                    borderColor = MaterialTheme.colorScheme.symptomColor.copy(alpha = 0.3f),
                    backgroundColor = MaterialTheme.colorScheme.symptomColor.copy(alpha = 0.05f)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        parsedAllergens.forEach { allergen ->
                            val displayRes = FoodAllergenHelper.keyToDisplayRes[allergen]
                            Pill(
                                text = if (displayRes != null) stringResource(displayRes) else allergen,
                                color = MaterialTheme.colorScheme.symptomColor.copy(alpha = 0.15f),
                                textColor = MaterialTheme.colorScheme.symptomColor
                            )
                        }
                    }
                }
            } else {
                AppCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.okColor, modifier = Modifier.size(20.dp))
                        Text(
                            stringResource(R.string.no_allergens_detected),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.label_notes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = MonoFontFamily
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text(stringResource(R.string.notes_placeholder_food), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton(
                    text = stringResource(R.string.btn_save_record),
                    onClick = {
                        viewModel.saveFood(
                            food = RecognizedFood(name = foodName, allergens = parsedAllergens),
                            notes = notes
                        )
                    }
                )
                SecondaryButton(
                    text = stringResource(R.string.btn_edit_selection),
                    onClick = { navController.popBackStack() }
                )
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }
}
