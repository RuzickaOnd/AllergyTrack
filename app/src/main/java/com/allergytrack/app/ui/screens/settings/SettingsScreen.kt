package com.allergytrack.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.allergytrack.app.BuildConfig
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.navigation.Screen
import com.allergytrack.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontFamily = SerifFontFamily) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // General Settings
            SettingsHeader(title = "GENERAL")
            
            AppCard {
                SettingsSwitchItem(
                    label = "Dark Mode (Forced)",
                    checked = true,
                    onCheckedChange = { /* Always dark in MVP */ }
                )
                HorizontalDivider(color = Border.copy(alpha = 0.5f), thickness = 0.5.dp)
                SettingsSwitchItem(
                    label = "Push Notifications",
                    checked = true,
                    onCheckedChange = { /* TODO */ }
                )
            }

            SettingsHeader(title = "DATA & PRIVACY")
            
            AppCard {
                SettingsActionItem(
                    label = "Export My Data",
                    onClick = { /* TODO */ }
                )
                HorizontalDivider(color = Border.copy(alpha = 0.5f), thickness = 0.5.dp)
                SettingsActionItem(
                    label = "Clear All Records",
                    onClick = { /* TODO: Show confirmation dialog */ }
                )
            }

            if (BuildConfig.DEBUG) {
                SettingsHeader(title = "DEVELOPER")
                AppCard {
                    SettingsActionItem(
                        label = "Debug Menu",
                        icon = Icons.Default.Build,
                        onClick = { navController.navigate(Screen.DebugMenu.route) }
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
            Text(
                text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.labelSmall,
                color = TextDim,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontFamily = MonoFontFamily
            )
        }
    }
}

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = TextDim,
        fontWeight = FontWeight.Bold,
        fontFamily = MonoFontFamily,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsSwitchItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label, 
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AccentGreen,
                checkedTrackColor = AccentGreen.copy(alpha = 0.3f),
                uncheckedThumbColor = TextDim,
                uncheckedTrackColor = Border
            )
        )
    }
}

@Composable
fun SettingsActionItem(
    label: String,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = AccentBlue)
            }
            Text(
                label, 
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Spacer(Modifier.weight(1f))
            Text("›", style = MaterialTheme.typography.headlineSmall, color = TextDim)
        }
    }
}
