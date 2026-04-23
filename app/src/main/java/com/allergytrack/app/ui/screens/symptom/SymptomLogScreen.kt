package com.allergytrack.app.ui.screens.symptom

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.allergytrack.app.domain.model.SymptomType
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.navigation.Screen
import com.allergytrack.app.ui.theme.*

// Definice částí těla v češtině
private val BODY_PARTS = listOf(
    "Obličej", "Krk", "Hrudník", "Záda",
    "Paže", "Ruce", "Břicho", "Nohy", "Chodidla"
)

@Composable
fun SymptomLogScreen(
    navController: NavController,
    viewModel: SymptomLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigace po uložení
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.reset()
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Dashboard.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // 1. Typ příznaku
        SymptomTypeSection(
            selectedType = uiState.selectedType,
            onTypeSelected = viewModel::onTypeSelected
        )

        // 2. Intenzita
        if (uiState.selectedType != null) {
            IntensitySection(
                intensity = uiState.intensity,
                onIntensityChanged = viewModel::onIntensityChanged
            )

            // 3. Části těla
            BodyPartSection(
                selectedParts = uiState.selectedBodyParts,
                onPartToggled = viewModel::onBodyPartToggled
            )

            // 4. Poznámky
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "POZNÁMKY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDim,
                    fontFamily = MonoFontFamily
                )
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChanged,
                    placeholder = { Text("Specifické spouštěče nebo detaily...", color = TextDim) },
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

            // 5. Uložit
            PrimaryButton(
                text = if (uiState.isSaving) "UKLÁDÁM..." else "ULOŽIT PŘÍZNAK",
                onClick = viewModel::saveSymptom,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            )
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
fun SymptomTypeSection(
    selectedType: SymptomType?,
    onTypeSelected: (SymptomType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CardTitle("TYP PŘÍZNAKU")
        
        // Mřížka 2 sloupce
        val types = SymptomType.entries
        for (row in types.chunked(2)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { type ->
                    SymptomTypeCard(
                        type = type,
                        isSelected = selectedType == type,
                        onClick = { onTypeSelected(type) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Vyrovnání pokud je lichý počet
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SymptomTypeCard(
    type: SymptomType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) AccentOrange else Border

    AppCard(
        onClick = onClick,
        modifier = modifier,
        borderColor = borderColor.copy(alpha = if (isSelected) 1f else 0.5f),
        backgroundColor = if (isSelected) AccentOrange.copy(alpha = 0.1f) else Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(type.emoji, style = MaterialTheme.typography.headlineSmall)
            Text(
                type.label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontFamily = MonoFontFamily,
                color = if (isSelected) AccentOrange else TextPrimary
            )
        }
    }
}

@Composable
fun IntensitySection(
    intensity: Int,
    onIntensityChanged: (Int) -> Unit
) {
    val intensityLabels = mapOf(
        1 to "Mírná",
        2 to "Lehká",
        3 to "Střední",
        4 to "Silná",
        5 to "Kritická"
    )
    val intensityColors = listOf(
        AccentGreen,
        Color(0xFF8BC34A),
        Color(0xFFFFC107),
        AccentOrange,
        DotRed
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            CardTitle("INTENZITA")
            Text(
                "${intensityLabels[intensity]} ($intensity/5)".uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = intensityColors[intensity - 1],
                fontFamily = MonoFontFamily
            )
        }

        // 5 tlačítek
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (1..5).forEach { level ->
                val isSelected = intensity >= level
                val color = intensityColors[level - 1]
                
                Surface(
                    onClick = { onIntensityChanged(level) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) color.copy(alpha = 0.2f) else Surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, 
                        if (intensity == level) color else Border.copy(alpha = 0.5f)
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$level",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (intensity == level) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) color else TextDim,
                            fontFamily = MonoFontFamily
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BodyPartSection(
    selectedParts: Set<String>,
    onPartToggled: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CardTitle("ZASAŽENÁ OBLAST")
        
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BODY_PARTS.forEach { part ->
                val isSelected = part in selectedParts
                FilterChip(
                    selected = isSelected,
                    onClick = { onPartToggled(part) },
                    label = { 
                        Text(
                            part.uppercase(), 
                            fontFamily = MonoFontFamily,
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Surface,
                        selectedContainerColor = AccentBlue.copy(alpha = 0.2f),
                        labelColor = TextDim,
                        selectedLabelColor = AccentBlue
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Border.copy(alpha = 0.5f),
                        selectedBorderColor = AccentBlue,
                        borderWidth = 1.dp
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}
