package com.allergytrack.app.ui.screens.symptom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Sick
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.compose.material3.Icon
import com.allergytrack.app.R
import com.allergytrack.app.ui.util.icon
import com.allergytrack.shared.domain.model.SymptomType
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.navigation.Screen
import com.allergytrack.app.ui.theme.*
import com.allergytrack.app.ui.util.BODY_PART_RES
import com.allergytrack.app.ui.util.labelRes

// Interní klíče pro části těla (uloženy v DB jako Czech strings pro zpětnou kompatibilitu)
private val BODY_PARTS = listOf(
    "Obličej", "Krk", "Hrudník", "Záda",
    "Paže", "Ruce", "Břicho", "Nohy", "Chodidla"
)

@Composable
fun SymptomLogScreen(
    navController: NavController,
    viewModel: SymptomLogViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.reset()
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Dashboard.route) { inclusive = true }
            }
        }
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    if (isTablet) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Levý sloupec: výběr typu příznaku
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                SymptomTypeSection(
                    selectedType = uiState.selectedType,
                    onTypeSelected = viewModel::onTypeSelected
                )
            }

            // Pravý sloupec: detaily příznaku
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                if (uiState.selectedType != null) {
                    IntensitySection(
                        intensity = uiState.intensity,
                        onIntensityChanged = viewModel::onIntensityChanged
                    )

                    BodyPartSection(
                        selectedParts = uiState.selectedBodyParts,
                        onPartToggled = viewModel::onBodyPartToggled
                    )

                    DateTimePickerRow(
                        timestamp = uiState.timestamp,
                        onTimestampChanged = viewModel::onTimestampChanged,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.label_notes),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = MonoFontFamily
                        )
                        OutlinedTextField(
                            value = uiState.notes,
                            onValueChange = viewModel::onNotesChanged,
                            placeholder = { Text(stringResource(R.string.notes_placeholder_symptom), color = MaterialTheme.colorScheme.onSurfaceVariant) },
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

                    val savingLabel = if (uiState.isSaving)
                        stringResource(R.string.btn_saving)
                    else
                        stringResource(R.string.btn_save_symptom)

                    PrimaryButton(
                        text = savingLabel,
                        onClick = viewModel::saveSymptom,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.select_symptom_type_prompt),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SymptomTypeSection(
                selectedType = uiState.selectedType,
                onTypeSelected = viewModel::onTypeSelected
            )

            if (uiState.selectedType != null) {
                IntensitySection(
                    intensity = uiState.intensity,
                    onIntensityChanged = viewModel::onIntensityChanged
                )

                BodyPartSection(
                    selectedParts = uiState.selectedBodyParts,
                    onPartToggled = viewModel::onBodyPartToggled
                )

                DateTimePickerRow(
                    timestamp = uiState.timestamp,
                    onTimestampChanged = viewModel::onTimestampChanged,
                    modifier = Modifier.fillMaxWidth()
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.label_notes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = MonoFontFamily
                    )
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = viewModel::onNotesChanged,
                        placeholder = { Text(stringResource(R.string.notes_placeholder_symptom), color = MaterialTheme.colorScheme.onSurfaceVariant) },
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

                val savingLabel = if (uiState.isSaving)
                    stringResource(R.string.btn_saving)
                else
                    stringResource(R.string.btn_save_symptom)

                PrimaryButton(
                    text = savingLabel,
                    onClick = viewModel::saveSymptom,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SymptomTypeSection(
    selectedType: SymptomType?,
    onTypeSelected: (SymptomType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CardTitle(stringResource(R.string.section_symptom_type), icon = Icons.Outlined.Sick)

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
    val borderColor = if (isSelected) MaterialTheme.colorScheme.warningColor else MaterialTheme.colorScheme.outline

    AppCard(
        onClick = onClick,
        modifier = modifier,
        borderColor = borderColor.copy(alpha = if (isSelected) 1f else 0.5f),
        backgroundColor = if (isSelected) MaterialTheme.colorScheme.warningColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = type.icon(),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.warningColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(type.labelRes()).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontFamily = MonoFontFamily,
                color = if (isSelected) MaterialTheme.colorScheme.warningColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun IntensitySection(
    intensity: Int,
    onIntensityChanged: (Int) -> Unit
) {
    val intensityLabels = listOf(
        stringResource(R.string.intensity_1),
        stringResource(R.string.intensity_2),
        stringResource(R.string.intensity_3),
        stringResource(R.string.intensity_4),
        stringResource(R.string.intensity_5)
    )
    val intensityColors = listOf(
        MaterialTheme.colorScheme.okColor,
        MaterialTheme.colorScheme.okColor,
        MaterialTheme.colorScheme.warningColor,
        MaterialTheme.colorScheme.warningColor,
        MaterialTheme.colorScheme.symptomColor
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            CardTitle(stringResource(R.string.section_intensity), icon = Icons.Outlined.Equalizer)
            Text(
                stringResource(R.string.intensity_format, intensityLabels[intensity - 1], intensity).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = intensityColors[intensity - 1],
                fontFamily = MonoFontFamily
            )
        }

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
                    color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (intensity == level) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$level",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (intensity == level) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
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
        CardTitle(stringResource(R.string.section_body_area), icon = Icons.Outlined.Person)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BODY_PARTS.forEach { part ->
                val isSelected = part in selectedParts
                val displayLabel = BODY_PART_RES[part]?.let { stringResource(it) } ?: part

                FilterChip(
                    selected = isSelected,
                    onClick = { onPartToggled(part) },
                    label = {
                        Text(
                            displayLabel.uppercase(),
                            fontFamily = MonoFontFamily,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        borderWidth = 1.dp
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}
