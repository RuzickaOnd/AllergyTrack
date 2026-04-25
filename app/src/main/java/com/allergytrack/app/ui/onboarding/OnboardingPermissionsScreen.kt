package com.allergytrack.app.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allergytrack.app.R
import com.allergytrack.app.domain.model.OnboardingStep
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingPermissionsScreen(
    uiState: OnboardingUiState,
    onPermissionsRefreshed: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val permissions = uiState.permissionsState

    // GPS Launcher
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { onPermissionsRefreshed() }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { onPermissionsRefreshed() }

    // Notification Launcher (Android 13+)
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { onPermissionsRefreshed() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.onboarding_permissions_title), fontFamily = SerifFontFamily) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.nav_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Subtitle
                Text(
                    text = stringResource(R.string.onboarding_permissions_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // GPS — Required for environmental data
                PermissionCard(
                    icon = Icons.Default.LocationOn,
                    title = stringResource(R.string.permission_location_title),
                    description = stringResource(R.string.permission_location_desc),
                    isGranted = permissions.location,
                    onRequest = {
                        locationLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )

                // Camera — Optional (for photo logging)
                PermissionCard(
                    icon = Icons.Default.CameraAlt,
                    title = stringResource(R.string.permission_camera_title),
                    description = stringResource(R.string.permission_camera_desc),
                    isGranted = permissions.camera,
                    onRequest = {
                        cameraLauncher.launch(Manifest.permission.CAMERA)
                    }
                )

                // Notifications — Optional (reminders)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionCard(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.permission_notifications_title),
                        description = stringResource(R.string.permission_notifications_desc),
                        isGranted = permissions.notifications,
                        onRequest = {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    )
                }

                // Privacy Info
                AppCard(borderColor = MaterialTheme.colorScheme.outline) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.permission_privacy_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OnboardingProgressDots(
                    currentStep = OnboardingStep.PERMISSIONS,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OnboardingBottomBar(
                    onNext = onNext,
                    onBack = onBack,
                    nextLabel = if (permissions.location) stringResource(R.string.btn_continue_arrow) else stringResource(R.string.btn_continue_no_location),
                )
            }
        }
    }
}

@Composable
fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    AppCard(
        borderColor = if (isGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, null,
                    tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                    lineHeight = 18.sp
                )
            }

            // State / Action
            if (isGranted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                IconButton(
                    onClick = onRequest,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Grant",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
