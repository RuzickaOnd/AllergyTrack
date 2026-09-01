package com.allergytrack.app.ui.screens.food

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.allergytrack.app.R
import com.allergytrack.shared.domain.model.PremiumFeature
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.navigation.Screen
import com.allergytrack.app.ui.screens.profile.PremiumActivity
import com.allergytrack.app.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodLogScreen(
    navController: NavController,
    viewModel: FoodLogViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val recentLogs by viewModel.recentFoodLogs.collectAsStateWithLifecycle()
    val isPhotoVisible by viewModel.isPhotoVisible.collectAsStateWithLifecycle()
    val showPhotoUpsell by viewModel.showPhotoUpsell.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val navigateToSearch by viewModel.navigateToSearch.collectAsStateWithLifecycle()
    val voicePermissionDeniedMsg = stringResource(R.string.voice_permission_denied)
    val voiceNotAvailableMsg = stringResource(R.string.voice_not_available)

    // SpeechRecognizer — lifecycle spravovaný DisposableEffect
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context))
            SpeechRecognizer.createSpeechRecognizer(context)
        else null
    }
    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        }
    }

    // Spustí naslouchání jakmile ViewModel přejde do stavu Listening
    LaunchedEffect(voiceState) {
        if (voiceState is VoiceInputState.Listening && speechRecognizer != null) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onResults(bundle: Bundle) {
                    val text = bundle
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull() ?: ""
                    if (text.isNotEmpty()) viewModel.onVoiceResult(text)
                    else viewModel.dismissVoiceInput()
                }
                override fun onError(error: Int) { viewModel.dismissVoiceInput() }
                override fun onReadyForSpeech(params: Bundle) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            speechRecognizer.startListening(intent)
        }
    }

    // Navigace do vyhledávání po rozpoznání hlasu
    LaunchedEffect(navigateToSearch) {
        if (navigateToSearch) {
            val encodedQuery = Uri.encode(viewModel.searchState.value.query)
            viewModel.onSearchNavigated()
            navController.navigate(Screen.FoodManualSearch.route(encodedQuery))
        }
    }

    // Permission launcher pro mikrofon
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startVoiceInput()
        } else {
            Toast.makeText(context, voicePermissionDeniedMsg, Toast.LENGTH_SHORT).show()
        }
    }

    // Dialog aktivního naslouchání
    if (voiceState is VoiceInputState.Listening) {
        AlertDialog(
            onDismissRequest = {
                speechRecognizer?.stopListening()
                viewModel.dismissVoiceInput()
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text(stringResource(R.string.voice_listening_title)) },
            text = {
                Text(
                    text = stringResource(R.string.voice_listening_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    speechRecognizer?.stopListening()
                    viewModel.dismissVoiceInput()
                }) {
                    Text(stringResource(R.string.btn_close))
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        item {
            PremiumGate(
                isVisible = isPhotoVisible,
                showUpsell = showPhotoUpsell,
                feature = PremiumFeature.FOOD_PHOTO_RECOGNITION,
                onUpgradeClick = { context.startActivity(Intent(context, PremiumActivity::class.java)) }
            ) {
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    onClick = { navController.navigate(Screen.Camera.photo()) }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(Modifier.fillMaxSize().padding(8.dp)) {
                            val cornerSize = 24.dp
                            val thickness = 3.dp
                            Box(Modifier.size(cornerSize).border(thickness, MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 8.dp)))
                            Box(Modifier.align(Alignment.TopEnd).size(cornerSize).border(thickness, MaterialTheme.colorScheme.primary, RoundedCornerShape(topEnd = 8.dp)))
                            Box(Modifier.align(Alignment.BottomStart).size(cornerSize).border(thickness, MaterialTheme.colorScheme.primary, RoundedCornerShape(bottomStart = 8.dp)))
                            Box(Modifier.align(Alignment.BottomEnd).size(cornerSize).border(thickness, MaterialTheme.colorScheme.primary, RoundedCornerShape(bottomEnd = 8.dp)))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.btn_scan_tap),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = MonoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.btn_scan_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(
                    text = stringResource(R.string.btn_barcode),
                    onClick = { navController.navigate(Screen.Camera.barcode()) },
                    modifier = Modifier.weight(1f)
                )
                SecondaryButton(
                    text = stringResource(R.string.btn_voice_input),
                    onClick = {
                        when {
                            !SpeechRecognizer.isRecognitionAvailable(context) ->
                                Toast.makeText(context, voiceNotAvailableMsg, Toast.LENGTH_SHORT).show()
                            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED ->
                                viewModel.startVoiceInput()
                            else ->
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            AppCard(
                borderColor = MaterialTheme.colorScheme.outline,
                onClick = { navController.navigate(Screen.FoodManualSearch.route()) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.search_food_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CardTitle(stringResource(R.string.section_recently_logged), icon = Icons.Outlined.History)
                if (recentLogs.isEmpty()) {
                    Text(
                        text = stringResource(R.string.food_recently_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recentLogs.forEach { log ->
                            val encodedName = Uri.encode(log.foodName)
                            val encodedAllergens = Uri.encode(log.allergens.joinToString("|"))
                            val onClick = {
                                navController.navigate(
                                    Screen.FoodConfirm.createRoute(encodedName, encodedAllergens)
                                )
                            }
                            if (log.allergens.isNotEmpty()) {
                                Pill(
                                    text = log.foodName,
                                    color = MaterialTheme.colorScheme.symptomColor.copy(alpha = 0.1f),
                                    textColor = MaterialTheme.colorScheme.symptomColor,
                                    onClick = onClick
                                )
                            } else {
                                Pill(text = log.foodName, onClick = onClick)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}
