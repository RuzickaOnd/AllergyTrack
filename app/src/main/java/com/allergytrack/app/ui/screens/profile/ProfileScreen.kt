package com.allergytrack.app.ui.screens.profile

import com.allergytrack.shared.presentation.*
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.allergytrack.app.BuildConfig
import com.allergytrack.shared.resources.*
import com.allergytrack.app.ui.components.AppCard
import com.allergytrack.app.ui.navigation.Screen
import com.allergytrack.app.ui.screens.settings.SettingsActionItem
import com.allergytrack.app.ui.screens.settings.SettingsHeader

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val avatarSelection by viewModel.avatarSelection.collectAsStateWithLifecycle()
    val isPremiumUser by viewModel.isPremiumUser.collectAsStateWithLifecycle()
    val isPremiumVisible by viewModel.isPremiumVisible.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var isEditingName by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isEditingName) {
        if (isEditingName) {
            editName = userName
            focusRequester.requestFocus()
        }
    }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        // Avatar with edit badge
        item {
            Box {
                AvatarCircle(
                    selection = avatarSelection,
                    size = 100.dp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            context.startActivity(Intent(context, AvatarPickerActivity::class.java))
                        }
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .size(30.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.background),
                    onClick = { context.startActivity(Intent(context, AvatarPickerActivity::class.java)) },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(Res.string.profile_change_avatar),
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }

        // Name — inline editing
        item {
            if (isEditingName) {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.headlineSmall,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        viewModel.setUserName(editName)
                        isEditingName = false
                        keyboardController?.hide()
                    }),
                    trailingIcon = {
                        IconButton(onClick = {
                            viewModel.setUserName(editName)
                            isEditingName = false
                            keyboardController?.hide()
                        }) {
                            Icon(Icons.Default.Done, null)
                        }
                    }
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isEditingName = true }
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = userName.ifEmpty { stringResource(Res.string.profile_name_placeholder) },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (userName.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        // Options list
        item {
            AppCard(contentPadding = PaddingValues(0.dp)) {
                if (isPremiumVisible) {
                    SettingsActionItem(
                        icon = Icons.Default.Star,
                        label = stringResource(
                            if (isPremiumUser) Res.string.profile_option_premium_manage
                            else Res.string.profile_option_premium
                        ),
                        onClick = { context.startActivity(Intent(context, PremiumActivity::class.java)) }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        thickness = 0.5.dp
                    )
                }
                SettingsActionItem(
                    icon = Icons.Default.Settings,
                    label = stringResource(Res.string.screen_settings),
                    onClick = { navController.navigate(Screen.Settings.route) }
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
                SettingsActionItem(
                    icon = Icons.Outlined.HelpOutline,
                    label = stringResource(Res.string.screen_faq),
                    onClick = { navController.navigate(Screen.Faq.route) }
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
                SettingsActionItem(
                    icon = Icons.Default.Info,
                    label = stringResource(Res.string.screen_about),
                    onClick = { navController.navigate(Screen.About.route) }
                )
            }
        }

        item {
            Text(
                stringResource(Res.string.about_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

