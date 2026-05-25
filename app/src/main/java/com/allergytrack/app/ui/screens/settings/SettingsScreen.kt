package com.allergytrack.app.ui.screens.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import android.widget.Toast
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.allergytrack.app.BuildConfig
import com.allergytrack.app.R
import com.allergytrack.app.ui.components.AppCard
import com.allergytrack.app.ui.components.AppSnackbarHost
import com.allergytrack.app.ui.components.PremiumBadge
import com.allergytrack.app.ui.screens.profile.PremiumActivity
import com.allergytrack.app.ui.navigation.Screen
import com.allergytrack.app.ui.theme.MonoFontFamily
import com.allergytrack.app.ui.util.sharePdfFile
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissions by viewModel.permissionsState.collectAsStateWithLifecycle()
    val foodLanguage by viewModel.foodSearchLanguage.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val restartAppRequired by viewModel.restartAppRequired.collectAsStateWithLifecycle()
    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val syncChartsScroll by viewModel.syncChartsScroll.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val exportedFile by viewModel.exportedFile.collectAsStateWithLifecycle()
    val isExportVisible by viewModel.isExportVisible.collectAsStateWithLifecycle()
    val canUseExport by viewModel.canUseExport.collectAsStateWithLifecycle()
    val showExportBadge by viewModel.showExportBadge.collectAsStateWithLifecycle()
    val canUseExtendedCharts by viewModel.canUseExtendedCharts.collectAsStateWithLifecycle()
    val fcmToken by viewModel.fcmToken.collectAsStateWithLifecycle()
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAppLanguageDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(exportedFile) {
        exportedFile?.let { file ->
            context.sharePdfFile(file)
            viewModel.dismissExport()
        }
    }

    // Pre-API 33: LocaleManager není dostupný — musíme recreate spustit ručně
    LaunchedEffect(restartAppRequired) {
        if (restartAppRequired) {
            (context as? Activity)?.recreate()
            viewModel.onRestartHandled()
        }
    }

    // Refresh permissions when returning to this screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // GPS Launcher
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { viewModel.refreshPermissions() }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { viewModel.refreshPermissions() }

    // Notification Launcher (Android 13+)
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { viewModel.refreshPermissions() }

    fun handlePermissionClick(granted: Boolean, permissionType: PermissionType, request: () -> Unit) {
        if (granted) {
            when (permissionType) {
                PermissionType.NOTIFICATIONS -> openNotificationSettings(context)
                else -> openAppSettings(context)
            }
        } else {
            request()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SettingsHeader(title = stringResource(R.string.settings_section_general))

            AppCard(contentPadding = PaddingValues(0.dp)) {
                val appLangLabel = when (appLanguage) {
                    "cs" -> "Čeština"
                    "en" -> "English"
                    else -> stringResource(R.string.settings_app_language_auto)
                }
                SettingsActionItem(
                    label = stringResource(R.string.settings_app_language),
                    sublabel = appLangLabel,
                    onClick = { showAppLanguageDialog = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                SettingsSwitchItem(
                    label = stringResource(R.string.settings_dark_mode),
                    checked = darkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                val permissionHint = stringResource(R.string.settings_notifications_permission_required)
                SettingsSwitchItem(
                    label = stringResource(R.string.settings_push_notifications),
                    checked = notificationsEnabled && permissions.notifications,
                    enabled = permissions.notifications,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                    onDisabledClick = {
                        scope.launch { snackbarHostState.showSnackbar(permissionHint) }
                    }
                )
            }

            if (canUseExtendedCharts) {
                SettingsHeader(title = stringResource(R.string.settings_section_charts))

                AppCard(contentPadding = PaddingValues(0.dp)) {
                    SettingsSwitchItem(
                        label = stringResource(R.string.settings_sync_charts_scroll),
                        checked = syncChartsScroll,
                        onCheckedChange = { viewModel.setSyncChartsScroll(it) }
                    )
                }
            }

            SettingsHeader(title = stringResource(R.string.settings_section_food_search))

            AppCard(contentPadding = PaddingValues(0.dp)) {
                val autoLabel = stringResource(
                    R.string.settings_food_language_auto,
                    LocalLocale.current.platformLocale.language.uppercase()
                )
                val currentLabel = if (foodLanguage.isEmpty()) autoLabel
                else FOOD_LANGUAGE_OPTIONS.find { it.code == foodLanguage }?.nativeName ?: autoLabel
                SettingsActionItem(
                    label = stringResource(R.string.settings_food_language),
                    sublabel = currentLabel,
                    onClick = { showLanguageDialog = true }
                )
            }

            if (showLanguageDialog) {
                FoodLanguageDialog(
                    current = foodLanguage,
                    onSelect = { code ->
                        viewModel.setFoodSearchLanguage(code)
                        showLanguageDialog = false
                    },
                    onDismiss = { showLanguageDialog = false }
                )
            }

            if (showAppLanguageDialog) {
                AppLanguageDialog(
                    current = appLanguage,
                    onSelect = { code ->
                        viewModel.setAppLanguage(code)
                        showAppLanguageDialog = false
                    },
                    onDismiss = { showAppLanguageDialog = false }
                )
            }

            SettingsHeader(title = stringResource(R.string.settings_section_permissions))

            AppCard(contentPadding = PaddingValues(0.dp)) {
                PermissionSettingItem(
                    label = stringResource(R.string.permission_location_title),
                    isGranted = permissions.location,
                    onClick = {
                        handlePermissionClick(permissions.location, PermissionType.LOCATION) {
                            locationLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                PermissionSettingItem(
                    label = stringResource(R.string.permission_camera_title),
                    isGranted = permissions.camera,
                    onClick = {
                        handlePermissionClick(permissions.camera, PermissionType.CAMERA) {
                            cameraLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                    PermissionSettingItem(
                        label = stringResource(R.string.permission_notifications_title),
                        isGranted = permissions.notifications,
                        onClick = {
                            handlePermissionClick(permissions.notifications, PermissionType.NOTIFICATIONS) {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                }
            }

            SettingsHeader(title = stringResource(R.string.settings_section_data_privacy))

            if (isExportVisible) {
                AppCard(contentPadding = PaddingValues(0.dp)) {
                    SettingsActionItem(
                        label = stringResource(R.string.settings_export_data),
                        sublabel = if (isExporting) stringResource(R.string.settings_exporting) else null,
                        badge = if (showExportBadge) { { PremiumBadge() } } else null,
                        onClick = {
                            if (canUseExport) {
                                if (!isExporting) viewModel.exportData()
                            } else {
                                context.startActivity(Intent(context, PremiumActivity::class.java))
                            }
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                    SettingsActionItem(
                        label = stringResource(R.string.settings_delete_data),
                        onClick = { showDeleteConfirmDialog = true }
                    )
                }
            } else {
                AppCard(contentPadding = PaddingValues(0.dp)) {
                    SettingsActionItem(
                        label = stringResource(R.string.settings_delete_data),
                        onClick = { showDeleteConfirmDialog = true }
                    )
                }
            }

            if (showDeleteConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text(stringResource(R.string.settings_delete_confirm_title)) },
                    text = { Text(stringResource(R.string.settings_delete_confirm_message)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteAllData()
                                showDeleteConfirmDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.settings_delete_confirm_btn))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = false }) {
                            Text(stringResource(R.string.btn_close))
                        }
                    }
                )
            }

            if (BuildConfig.DEBUG) {
                SettingsHeader(title = stringResource(R.string.settings_section_developer))
                AppCard(contentPadding = PaddingValues(0.dp)) {
                    SettingsActionItem(
                        label = stringResource(R.string.settings_debug_menu_label),
                        icon = Icons.Default.Build,
                        onClick = { navController.navigate(Screen.DebugMenu.route) }
                    )
                }
            }

            val fcmTokenText = when (val token = fcmToken) {
                null -> stringResource(R.string.settings_fcm_token_loading)
                "" -> stringResource(R.string.settings_fcm_token_unavailable)
                else -> token
            }

            SettingsHeader(title = stringResource(R.string.settings_section_diagnostics))
            AppCard(contentPadding = PaddingValues(0.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_fcm_token_title),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = fcmTokenText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = MonoFontFamily,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.MiddleEllipsis
                        )
                    }
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                    androidx.compose.material3.IconButton(
                        onClick = {
                            val token = fcmToken
                            if (!token.isNullOrEmpty()) {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(token))
                                Toast.makeText(context, context.getString(R.string.settings_fcm_token_copied_toast), Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.settings_fcm_token_copy_desc),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
        AppSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun PermissionSettingItem(
    label: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (isGranted) stringResource(R.string.ready_status_granted) else stringResource(R.string.ready_status_denied),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private enum class PermissionType { LOCATION, CAMERA, NOTIFICATIONS }

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}

private fun openNotificationSettings(context: Context) {
    val intent =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    context.startActivity(intent)
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
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    onDisabledClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (enabled) onCheckedChange(!checked)
                else onDisabledClick?.invoke()
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
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
    sublabel: String? = null,
    badge: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (icon != null) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.secondary)
        }
        Column(Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                badge?.invoke()
            }
            if (sublabel != null) {
                Text(
                    sublabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AppLanguageDialog(
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        Triple("", stringResource(R.string.settings_app_language_auto), false),
        Triple("cs", "Čeština", false),
        Triple("en", "English", false),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_app_language_dialog_title)) },
        text = {
            Column {
                options.forEach { (code, label, _) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(code) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = code == current,
                            onClick = { onSelect(code) }
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_close))
            }
        }
    )
}

@Composable
fun FoodLanguageDialog(
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_food_language_dialog_title)) },
        text = {
            LazyColumn {
                items(FOOD_LANGUAGE_OPTIONS) { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option.code) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option.code == current,
                            onClick = { onSelect(option.code) }
                        )
                        Text(
                            text = option.nativeName,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_close))
            }
        }
    )
}
