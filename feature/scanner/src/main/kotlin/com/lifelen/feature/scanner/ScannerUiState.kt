package com.lifelen.feature.scanner

/** UI state for the camera/analyze screen. */
data class ScannerUiState(
    val isAnalyzing: Boolean = false,
    val hasVisionKey: Boolean = true,
    val error: String? = null,
)

/** One-shot navigation signals emitted after a successful scan. */
sealed interface ScannerEvent {
    data class ScanComplete(val scanId: String) : ScannerEvent
}
