package com.lifelen.feature.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifelen.core.common.result.DataResult
import com.lifelen.core.data.repository.ScanRepository
import com.lifelen.core.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ScannerEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.update { it.copy(hasVisionKey = settings.hasVisionKey) }
            }
        }
    }

    fun analyze(imageBytes: ByteArray) {
        if (_uiState.value.isAnalyzing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, error = null) }
            val options = settingsRepository.scanOptions()
            when (val result = scanRepository.identify(imageBytes, options)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isAnalyzing = false) }
                    _events.emit(ScannerEvent.ScanComplete(result.data.id))
                }

                is DataResult.Error -> _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        error = result.throwable.message ?: "Couldn't analyze that. Try again.",
                    )
                }

                DataResult.Loading -> Unit
            }
        }
    }

    fun onCaptureError(message: String) = _uiState.update { it.copy(error = message) }

    fun dismissError() = _uiState.update { it.copy(error = null) }
}
