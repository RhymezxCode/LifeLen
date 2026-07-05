package com.lifelen.feature.scanner

/**
 * UI state for the S02 camera home screen.
 *
 * Navigation is signalled out-of-band via [ScannerViewModel.events]; this holds only render state.
 */
data class ScannerUiState(
    val isCapturing: Boolean = false,
    val hasVisionKey: Boolean = true,
    val libraryCount: Int = 0,
    val lastThumbPath: String? = null,
    val selectedMode: String = "Auto",
    val error: String? = null,
)
