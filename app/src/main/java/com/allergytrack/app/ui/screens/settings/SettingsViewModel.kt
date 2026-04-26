package com.allergytrack.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.allergytrack.app.data.permissions.PermissionsHelper
import com.allergytrack.app.data.permissions.PermissionsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val permissionsHelper: PermissionsHelper
) : ViewModel() {

    private val _permissionsState = MutableStateFlow(permissionsHelper.getPermissionsState())
    val permissionsState: StateFlow<PermissionsState> = _permissionsState.asStateFlow()

    fun refreshPermissions() {
        _permissionsState.update { permissionsHelper.getPermissionsState() }
    }
}
