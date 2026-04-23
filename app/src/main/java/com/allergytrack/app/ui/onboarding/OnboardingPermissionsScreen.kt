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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allergytrack.app.domain.model.OnboardingStep
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.theme.*

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Header
            SectionHeading(
                title = "Required Access",
                subtitle = "To automatically track your environment and log meals. You can revoke any permission in settings anytime."
            )

            // GPS — Required for environmental data
            PermissionCard(
                icon = Icons.Default.LocationOn,
                title = "Location",
                description = "Automatically load pollen data and air quality based on where you move.",
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
                title = "Camera",
                description = "AI food recognition from photos. You can also log meals manually without camera.",
                isGranted = permissions.camera,
                onRequest = {
                    cameraLauncher.launch(Manifest.permission.CAMERA)
                }
            )

            // Notifications — Optional (reminders)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionCard(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    description = "Evening reminders and alerts for high pollen levels.",
                    isGranted = permissions.notifications,
                    onRequest = {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                )
            }

            // Privacy Info
            AppCard(borderColor = Border) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock, null,
                        tint = TextDim,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Your health data stays on your device. We never share it with third parties.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
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
                nextLabel = if (permissions.location) "CONTINUE →" else "CONTINUE WITHOUT LOCATION →",
            )
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
        borderColor = if (isGranted) AccentGreen.copy(alpha = 0.3f) else Border
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
                    .background(if (isGranted) AccentGreen.copy(alpha = 0.1f) else Border.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, null,
                    tint = if (isGranted) AccentGreen else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                    lineHeight = 18.sp
                )
            }

            // State / Action
            if (isGranted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = AccentGreen,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                IconButton(
                    onClick = onRequest,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AccentGreen)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Grant",
                        tint = Background,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
