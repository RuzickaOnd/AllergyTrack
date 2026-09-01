package com.allergytrack.app.ui.screens.profile

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.allergytrack.shared.resources.*

data class AvatarPreset(val id: String, val icon: ImageVector, val labelRes: StringResource)

val AVATAR_PRESETS = listOf(
    AvatarPreset("person", Icons.Default.Person, Res.string.avatar_person),
    AvatarPreset("favorite", Icons.Default.Favorite, Res.string.avatar_favorite),
    AvatarPreset("star", Icons.Default.Star, Res.string.avatar_star),
    AvatarPreset("pets", Icons.Default.Pets, Res.string.avatar_pets),
    AvatarPreset("local_florist", Icons.Default.LocalFlorist, Res.string.avatar_florist),
    AvatarPreset("fitness_center", Icons.Default.FitnessCenter, Res.string.avatar_fitness),
    AvatarPreset("directions_run", Icons.AutoMirrored.Filled.DirectionsRun, Res.string.avatar_run),
    AvatarPreset("spa", Icons.Default.Spa, Res.string.avatar_spa),
    AvatarPreset("eco", Icons.Default.Eco, Res.string.avatar_eco),
    AvatarPreset("health_and_safety", Icons.Default.HealthAndSafety, Res.string.avatar_health),
    AvatarPreset("sentiment_satisfied", Icons.Default.SentimentSatisfied, Res.string.avatar_happy),
    AvatarPreset("self_improvement", Icons.Default.SelfImprovement, Res.string.avatar_mindful),
)

@Composable
fun AvatarCircle(
    selection: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        if (selection.startsWith("gallery:")) {
            AsyncImage(
                model = selection.removePrefix("gallery:").toUri(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            val preset = AVATAR_PRESETS.find { it.id == selection } ?: AVATAR_PRESETS.first()
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = preset.icon,
                    contentDescription = null,
                    modifier = Modifier.size(size * 0.6f),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPickerScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val currentSelection by viewModel.avatarSelection.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.setAvatarSelection("gallery:$uri")
            onBack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.avatar_picker_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.nav_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(AVATAR_PRESETS) { preset ->
                    AvatarPresetItem(
                        preset = preset,
                        isSelected = currentSelection == preset.id,
                        onClick = {
                            viewModel.setAvatarSelection(preset.id)
                            onBack()
                        }
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.avatar_from_gallery))
            }
        }
    }
}

@Composable
private fun AvatarPresetItem(
    preset: AvatarPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                )
                .padding(2.dp)
        ) {
            Surface(
                onClick = onClick,
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = preset.icon,
                        contentDescription = stringResource(preset.labelRes),
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        Text(
            text = stringResource(preset.labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
