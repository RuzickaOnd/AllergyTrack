package com.allergytrack.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.allergytrack.app.BuildConfig
import com.allergytrack.app.R
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.navigation.Screen
import com.allergytrack.app.ui.theme.*

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        SettingsHeader(title = stringResource(R.string.settings_section_general))

        AppCard {
            SettingsSwitchItem(
                label = stringResource(R.string.settings_dark_mode),
                checked = true,
                onCheckedChange = { /* Always dark in MVP */ }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
            SettingsSwitchItem(
                label = stringResource(R.string.settings_push_notifications),
                checked = true,
                onCheckedChange = { /* TODO */ }
            )
        }

        SettingsHeader(title = stringResource(R.string.settings_section_data_privacy))

        AppCard {
            SettingsActionItem(
                label = stringResource(R.string.settings_export_data),
                onClick = { /* TODO */ }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
            SettingsActionItem(
                label = stringResource(R.string.settings_delete_data),
                onClick = { /* TODO: Show confirmation dialog */ }
            )
        }

        if (BuildConfig.DEBUG) {
            SettingsHeader(title = stringResource(R.string.settings_section_developer))
            AppCard {
                SettingsActionItem(
                    label = stringResource(R.string.settings_debug_menu_label),
                    icon = Icons.Default.Build,
                    onClick = { navController.navigate(Screen.DebugMenu.route) }
                )
            }
        }

        Spacer(Modifier.height(40.dp))
        Text(
            text = stringResource(R.string.settings_version_format, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE.toString()),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontFamily = MonoFontFamily
        )
    }
}

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline
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
                Icon(icon, null, tint = MaterialTheme.colorScheme.secondary)
            }
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.weight(1f))
            Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
