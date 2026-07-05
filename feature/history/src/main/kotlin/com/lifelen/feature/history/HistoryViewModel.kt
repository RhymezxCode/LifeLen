package com.lifelen.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifelen.core.data.repository.HistoryRepository
import com.lifelen.core.model.Scan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HistoryUiState(
    val query: String = "",
    val scans: List<Scan> = emptyList(),
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<HistoryUiState> = query
        .flatMapLatest { q ->
            val source = if (q.isBlank()) {
                historyRepository.observeHistory()
            } else {
                historyRepository.search(q)
            }
            source.map { scans -> HistoryUiState(query = q, scans = scans, isLoading = false) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(),
        )

    fun onQueryChange(value: String) {
        query.value = value
    }
}
