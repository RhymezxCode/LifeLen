package com.lifelen.feature.results

import com.lifelen.core.model.Scan

sealed interface ResultsUiState {
    data object Loading : ResultsUiState
    data object NotFound : ResultsUiState
    data class Success(val scan: Scan) : ResultsUiState
}
