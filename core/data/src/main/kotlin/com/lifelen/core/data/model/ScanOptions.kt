package com.lifelen.core.data.model

/** Per-scan toggles resolved from user settings before an analysis runs. */
data class ScanOptions(
    val pricingEnabled: Boolean = true,
)
