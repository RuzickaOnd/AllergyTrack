package com.allergytrack.shared.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface AppUpdateRepository {
    val isUpdateDownloaded: StateFlow<Boolean>
    fun setUpdateDownloaded(downloaded: Boolean, onComplete: (() -> Unit)?)
    fun completeUpdate()
}
