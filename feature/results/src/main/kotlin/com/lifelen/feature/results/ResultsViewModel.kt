package com.lifelen.feature.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifelen.core.data.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val scanId: String = checkNotNull(savedStateHandle["scanId"]) {
        "results destination requires a scanId argument"
    }

    private val _uiState = MutableStateFlow<ResultsUiState>(ResultsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val scan = historyRepository.getScan(scanId)
            _uiState.value = scan?.let { ResultsUiState.Success(it) } ?: ResultsUiState.NotFound
        }
    }

    fun toggleFavorite() {
        val scan = (_uiState.value as? ResultsUiState.Success)?.scan ?: return
        viewModelScope.launch {
            historyRepository.toggleFavorite(scan.id, !scan.isFavorite)
            load()
        }
    }
}
