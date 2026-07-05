package com.lifelen.feature.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifelen.core.data.repository.HistoryRepository
import com.lifelen.core.data.repository.SettingsRepository
import com.lifelen.core.data.session.ScanSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the S02 camera home. Owns capture → [ScanSession] hand-off and mirrors library/settings
 * state into [ScannerUiState]. A successful capture emits a one-shot [events] signal that the route
 * turns into navigation to the result screen.
 */
@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scanSession: ScanSession,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            historyRepository.observeHistory().collect { scans ->
                _uiState.update {
                    it.copy(
                        libraryCount = scans.size,
                        lastThumbPath = scans.firstOrNull()?.imagePath,
                    )
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.update { it.copy(hasVisionKey = settings.hasVisionKey) }
            }
        }
    }

    /** Persists the captured frame into the session and signals the route to open the result. */
    fun onCaptured(bytes: ByteArray) {
        if (_uiState.value.isCapturing) return
        _uiState.update { it.copy(isCapturing = true, error = null) }
        viewModelScope.launch {
            scanSession.beginCapture(bytes)
            _events.emit(Unit)
            _uiState.update { it.copy(isCapturing = false) }
        }
    }

    fun selectMode(mode: String) = _uiState.update { it.copy(selectedMode = mode) }

    fun onCaptureError(msg: String) = _uiState.update { it.copy(error = msg) }

    fun dismissError() = _uiState.update { it.copy(error = null) }
}
