package com.lifelen.feature.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifelen.core.common.result.DataResult
import com.lifelen.core.data.connectivity.NetworkMonitor
import com.lifelen.core.data.repository.HistoryRepository
import com.lifelen.core.data.repository.ScanRepository
import com.lifelen.core.data.repository.SettingsRepository
import com.lifelen.core.data.session.ScanSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Backs the result sheet for both flows:
 *  - Fresh capture (S03→S04/S06/S07): identifies [ScanSession.currentDraft] and shows the result.
 *  - Saved detail (S09): loads a persisted [Scan] by id.
 *
 * `scanId == "current"` (or a missing arg) means "fresh capture".
 */
@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val scanSession: ScanSession,
    private val networkMonitor: NetworkMonitor,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /** Null for a fresh capture; a library id for saved detail. "current" is treated as null. */
    private val scanId: String? =
        savedStateHandle.get<String>("scanId")?.takeUnless { it == "current" }

    private val _uiState = MutableStateFlow<ResultsUiState>(ResultsUiState.Processing)
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    /** The frozen frame the sheet sits over: draft path for a fresh scan, or the saved scan's image. */
    private val _capturedImagePath = MutableStateFlow<String?>(null)
    val capturedImagePath: StateFlow<String?> = _capturedImagePath.asStateFlow()

    private val _events = MutableSharedFlow<ResultEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ResultEvent> = _events.asSharedFlow()

    init {
        if (scanId != null) {
            loadSaved(scanId)
        } else {
            identifyFresh()
        }
    }

    private fun loadSaved(id: String) {
        viewModelScope.launch {
            val scan = historyRepository.getScan(id)
            if (scan != null) {
                _capturedImagePath.value = scan.imagePath
                _uiState.value = ResultsUiState.Ready(scan = scan, saved = true)
            } else {
                _uiState.value = ResultsUiState.NotFound
            }
        }
    }

    private fun identifyFresh() {
        val draft = scanSession.currentDraft()
        if (draft == null) {
            _uiState.value = ResultsUiState.Failed("No capture")
            return
        }
        _capturedImagePath.value = draft.imagePath
        _uiState.value = ResultsUiState.Processing
        viewModelScope.launch {
            when (val result = scanRepository.identify(draft, settingsRepository.scanOptions())) {
                is DataResult.Success -> {
                    scanSession.setResult(result.data)
                    _uiState.value = ResultsUiState.Ready(scan = result.data, saved = false)
                }

                is DataResult.Error ->
                    _uiState.value = if (!networkMonitor.isOnline()) {
                        // Offline: fall back to the most recent saved scan (history is newest-first).
                        ResultsUiState.Offline(
                            lastScan = historyRepository.observeHistory().first().firstOrNull(),
                        )
                    } else {
                        ResultsUiState.Failed(result.throwable.message ?: "Couldn't identify this")
                    }

                DataResult.Loading -> Unit
            }
        }
    }

    /** Persist the current scan to the library and surface a confirmation pill. */
    fun save() {
        val scan = (_uiState.value as? ResultsUiState.Ready)?.scan ?: return
        viewModelScope.launch {
            scanRepository.save(scan)
            _uiState.update { if (it is ResultsUiState.Ready) it.copy(saved = true) else it }
            _events.emit(ResultEvent.Saved)
        }
    }

    /** Fresh-capture top-right control — bounce back to the camera. */
    fun retake() {
        viewModelScope.launch { _events.emit(ResultEvent.Retake) }
    }

    /** Re-attempt a failed/offline fresh identification — the draft is still held by the session. */
    fun retry() = identifyFresh()

    /** Saved-detail top-right control — re-fetch live pricing and swap in the updated scan. */
    fun refresh() {
        val scan = (_uiState.value as? ResultsUiState.Ready)?.scan ?: return
        viewModelScope.launch {
            when (val result = scanRepository.refreshPrice(scan, settingsRepository.scanOptions())) {
                is DataResult.Success ->
                    _uiState.update { if (it is ResultsUiState.Ready) it.copy(scan = result.data) else it }

                else -> Unit
            }
        }
    }

    /** Saved-detail — flip the favourite flag, persist it, and reflect it optimistically. */
    fun toggleFavorite() {
        val scan = (_uiState.value as? ResultsUiState.Ready)?.scan ?: return
        val next = !scan.isFavorite
        viewModelScope.launch {
            historyRepository.toggleFavorite(scan.id, next)
            _uiState.update {
                if (it is ResultsUiState.Ready) it.copy(scan = it.scan.copy(isFavorite = next)) else it
            }
        }
    }

    /** Saved-detail — remove the scan from the library and pop back via [ResultEvent.Deleted]. */
    fun delete() {
        val scan = (_uiState.value as? ResultsUiState.Ready)?.scan ?: return
        viewModelScope.launch {
            historyRepository.delete(scan.id)
            _events.emit(ResultEvent.Deleted)
        }
    }

    /** Adjust the food portion multiplier (clamped 0.5..4 in 0.5 steps). */
    fun setPortion(factor: Float) {
        val clamped = ((factor / 0.5f).roundToInt() * 0.5f).coerceIn(0.5f, 4f)
        _uiState.update { if (it is ResultsUiState.Ready) it.copy(portionFactor = clamped) else it }
    }

    fun currentScanId(): String? = (_uiState.value as? ResultsUiState.Ready)?.scan?.id
}
