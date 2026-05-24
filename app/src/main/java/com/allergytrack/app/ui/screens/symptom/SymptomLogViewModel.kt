package com.allergytrack.app.ui.screens.symptom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allergytrack.app.domain.model.SymptomLog
import com.allergytrack.app.domain.model.SymptomType
import com.allergytrack.app.domain.usecase.SaveSymptomLogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SymptomLogUiState(
    val selectedType: SymptomType? = null,
    val intensity: Int = 3,
    val selectedBodyParts: Set<String> = emptySet(),
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SymptomLogViewModel @Inject constructor(
    private val saveSymptomLogUseCase: SaveSymptomLogUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SymptomLogUiState())
    val uiState: StateFlow<SymptomLogUiState> = _uiState.asStateFlow()

    fun onTypeSelected(type: SymptomType) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun onIntensityChanged(intensity: Int) {
        _uiState.update { it.copy(intensity = intensity.coerceIn(1, 5)) }
    }

    fun onBodyPartToggled(part: String) {
        _uiState.update { state ->
            val current = state.selectedBodyParts.toMutableSet()
            if (part in current) current.remove(part) else current.add(part)
            state.copy(selectedBodyParts = current)
        }
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onTimestampChanged(timestamp: Long) {
        _uiState.update { it.copy(timestamp = timestamp) }
    }

    fun saveSymptom() {
        val state = _uiState.value
        val type = state.selectedType ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                saveSymptomLogUseCase(
                    SymptomLog(
                        type = type,
                        intensity = state.intensity,
                        bodyParts = state.selectedBodyParts.toList(),
                        notes = state.notes,
                        timestamp = state.timestamp
                    )
                )
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun reset() {
        _uiState.value = SymptomLogUiState()
    }
}
