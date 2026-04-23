package com.allergytrack.app.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.allergytrack.app.data.remote.food.RecognizedFood
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.navigation.Screen
import com.allergytrack.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    // Navigate to Dashboard after save
    LaunchedEffect(uiState) {
        if (uiState is FoodLogUiState.Saved) {
            viewModel.reset()
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Dashboard.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm Food", fontFamily = SerifFontFamily) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // Recognized food card
            item {
                AppCard(
                    borderColor = AccentGreen.copy(alpha = 0.5f),
                    backgroundColor = AccentGreen.copy(alpha = 0.05f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(AccentGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🍽", fontSize = 28.sp)
                        }
                        Column {
                            Text(
                                text = foodName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "IDENTIFIED BY AI",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentGreen,
                                fontFamily = MonoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Allergens section
            item {
                CardTitle(text = "DETECTED ALLERGENS")
                if (parsedAllergens.isNotEmpty()) {
                    AppCard(
                        borderColor = DotRed.copy(alpha = 0.3f),
                        backgroundColor = DotRed.copy(alpha = 0.05f)
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            parsedAllergens.forEach { allergen ->
                                Pill(
                                    text = allergen.uppercase(),
                                    color = DotRed.copy(alpha = 0.15f),
                                    textColor = DotRed
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
                            Icon(Icons.Default.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                            Text(
                                "No common allergens detected",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Notes field
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "NOTES",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextDim,
                        fontFamily = MonoFontFamily
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Add details (e.g. homemade, ingredients)...", color = TextDim) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Surface,
                            focusedContainerColor = Surface,
                            unfocusedBorderColor = Border,
                            focusedBorderColor = AccentBlue
                        )
                    )
                }
            }

            // Action buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrimaryButton(
                        text = "SAVE LOG",
                        onClick = {
                            viewModel.saveFood(
                                food = RecognizedFood(
                                    name = foodName,
                                    allergens = parsedAllergens
                                ),
                                notes = notes
                            )
                        }
                    )

                    SecondaryButton(
                        text = "EDIT SELECTION",
                        onClick = { navController.popBackStack() }
                    )
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}
