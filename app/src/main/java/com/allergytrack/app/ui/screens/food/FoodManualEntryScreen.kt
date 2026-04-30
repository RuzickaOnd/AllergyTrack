package com.allergytrack.app.ui.screens.food

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.allergytrack.app.R
import com.allergytrack.app.data.remote.food.FoodAllergenHelper
import com.allergytrack.app.ui.components.AppCard
import com.allergytrack.app.ui.components.CardTitle
import com.allergytrack.app.ui.components.PrimaryButton
import com.allergytrack.app.ui.components.SectionHeading
import com.allergytrack.app.ui.navigation.Screen
import com.allergytrack.app.ui.theme.MonoFontFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodManualEntryScreen(
    navController: NavController,
    initialName: String,
    viewModel: FoodLogViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedAllergens by remember { mutableStateOf<Set<String>>(emptySet()) }
    val suggestedAllergens by viewModel.suggestedAllergens.collectAsStateWithLifecycle()
    val isSuggesting by viewModel.isSuggesting.collectAsStateWithLifecycle()

    // Při příchodu návrhů automaticky předvybere nově navržené alergeny
    LaunchedEffect(suggestedAllergens) {
        if (suggestedAllergens.isNotEmpty()) {
            selectedAllergens = suggestedAllergens
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearAllergenSuggestions() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        SectionHeading(title = stringResource(R.string.screen_manual_food_entry))

        AppCard {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.manual_entry_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        }

        AppCard {
            CardTitle(
                text = stringResource(R.string.manual_entry_allergen_heading),
                icon = Icons.Outlined.AutoAwesome
            )

            Spacer(Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FoodAllergenHelper.EU_14.forEach { allergen ->
                    val selected = allergen.key in selectedAllergens
                    FilterChip(
                        selected = selected,
                        onClick = {
                            selectedAllergens = if (selected) {
                                selectedAllergens - allergen.key
                            } else {
                                selectedAllergens + allergen.key
                            }
                        },
                        label = {
                            Text(
                                text = stringResource(allergen.displayRes),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = MonoFontFamily
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.suggestAllergens(name) },
                enabled = name.isNotBlank() && !isSuggesting,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSuggesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.manual_entry_suggesting))
                } else {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.manual_entry_suggest_btn))
                }
            }

            if (!isSuggesting && suggestedAllergens.isEmpty() && name.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.manual_entry_suggest_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        PrimaryButton(
            text = stringResource(R.string.btn_continue),
            enabled = name.isNotBlank(),
            onClick = {
                val encodedName = Uri.encode(name.trim())
                val encodedAllergens = Uri.encode(selectedAllergens.joinToString("|"))
                navController.navigate(Screen.FoodConfirm.createRoute(encodedName, encodedAllergens))
            }
        )

        Spacer(Modifier.height(24.dp))
    }
}
