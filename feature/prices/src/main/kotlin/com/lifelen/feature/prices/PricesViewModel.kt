package com.lifelen.feature.prices

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifelen.core.common.result.DataResult
import com.lifelen.core.data.repository.HistoryRepository
import com.lifelen.core.data.repository.ScanRepository
import com.lifelen.core.data.repository.SettingsRepository
import com.lifelen.core.data.session.ScanSession
import com.lifelen.core.model.PriceCondition
import com.lifelen.core.model.Scan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PricesViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val scanRepository: ScanRepository,
    private val settingsRepository: SettingsRepository,
    private val scanSession: ScanSession,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val scanId: String? = savedStateHandle["scanId"]

    /** The resolved scan, kept so [refresh] can re-fetch pricing after init. */
    private var scan: Scan? = null

    private val _uiState = MutableStateFlow(PricesUiState())
    val uiState: StateFlow<PricesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val resolved = scanId?.let { historyRepository.getScan(it) } ?: scanSession.currentResult()
            scan = resolved
            if (resolved == null || resolved.price == null) {
                _uiState.update { it.copy(notFound = true) }
            } else {
                _uiState.update { it.copy(title = resolved.title, price = resolved.price) }
            }
        }
    }

    fun selectCondition(condition: PriceCondition) {
        _uiState.update { it.copy(selectedCondition = condition) }
    }

    fun refresh() {
        val current = scan
        _uiState.update { it.copy(isRefreshing = true) }
        if (current == null) {
            _uiState.update { it.copy(isRefreshing = false) }
            return
        }
        viewModelScope.launch {
            when (val result = scanRepository.refreshPrice(current, settingsRepository.scanOptions())) {
                is DataResult.Success -> {
                    scan = result.data
                    _uiState.update { it.copy(price = result.data.price, isRefreshing = false) }
                }

                else -> _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }
}
