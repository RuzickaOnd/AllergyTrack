package com.allergytrack.app.data.repository

import com.allergytrack.app.domain.repository.AppUpdateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateRepositoryImpl @Inject constructor() : AppUpdateRepository {
    private val _isUpdateDownloaded = MutableStateFlow(false)
    override val isUpdateDownloaded: StateFlow<Boolean> = _isUpdateDownloaded.asStateFlow()

    private var completeUpdateAction: (() -> Unit)? = null

    override fun setUpdateDownloaded(downloaded: Boolean, onComplete: (() -> Unit)?) {
        _isUpdateDownloaded.value = downloaded
        completeUpdateAction = onComplete
    }

    override fun completeUpdate() {
        completeUpdateAction?.invoke()
    }
}
