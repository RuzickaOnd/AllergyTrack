package com.allergytrack.app.ui.util

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.allergytrack.app.BuildConfig
import com.allergytrack.shared.domain.repository.AppUpdateRepository
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo
import kotlinx.coroutines.launch

class AppUpdateHelper(
    private val activity: ComponentActivity,
    private val appUpdateRepository: AppUpdateRepository
) : DefaultLifecycleObserver {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private var isListenerRegistered = false

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            appUpdateRepository.setUpdateDownloaded(true) {
                appUpdateManager.completeUpdate()
            }
        }
    }

    private val updateLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            // Update flow failed or was cancelled by user
        }
    }

    init {
        activity.lifecycle.addObserver(this)
    }

    fun checkForUpdates() {
        if (BuildConfig.DEBUG) return // Pouze v release verzi

        activity.lifecycleScope.launch {
            try {
                val appUpdateInfo = appUpdateManager.requestAppUpdateInfo()
                when (appUpdateInfo.updateAvailability()) {
                    UpdateAvailability.UPDATE_AVAILABLE -> {
                        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                            startUpdate(appUpdateInfo, AppUpdateType.IMMEDIATE)
                        } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                            registerListener()
                            startUpdate(appUpdateInfo, AppUpdateType.FLEXIBLE)
                        }
                    }
                    else -> { /* Žádná aktualizace není k dispozici */ }
                }
            } catch (e: Exception) {
                // Selhání nebo offline stav ignorujeme pro plynulý chod aplikace
            }
        }
    }

    private fun startUpdate(appUpdateInfo: AppUpdateInfo, updateType: Int) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                updateLauncher,
                AppUpdateOptions.newBuilder(updateType).build()
            )
        } catch (e: Exception) {
            // Ignorujeme případné výjimky při spuštění flow
        }
    }

    private fun registerListener() {
        if (!isListenerRegistered) {
            appUpdateManager.registerListener(installStateUpdatedListener)
            isListenerRegistered = true
        }
    }

    private fun unregisterListener() {
        if (isListenerRegistered) {
            appUpdateManager.unregisterListener(installStateUpdatedListener)
            isListenerRegistered = false
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        if (BuildConfig.DEBUG) return

        // Kontrola případného obnovení rozpracované nebo stažené aktualizace
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startUpdate(appUpdateInfo, AppUpdateType.IMMEDIATE)
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                appUpdateRepository.setUpdateDownloaded(true) {
                    appUpdateManager.completeUpdate()
                }
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        unregisterListener()
    }
}
