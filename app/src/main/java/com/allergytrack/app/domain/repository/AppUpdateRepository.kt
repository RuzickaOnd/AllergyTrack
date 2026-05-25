package com.allergytrack.app.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface AppUpdateRepository {
    val isUpdateDownloaded: StateFlow<Boolean>
    fun setUpdateDownloaded(downloaded: Boolean, onComplete: (() -> Unit)?)
    fun completeUpdate()
}
