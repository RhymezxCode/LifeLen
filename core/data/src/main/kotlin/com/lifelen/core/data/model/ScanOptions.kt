package com.lifelen.core.data.model

import com.lifelen.core.common.location.Region

/** Per-scan toggles resolved from user settings before an analysis runs. */
data class ScanOptions(
    val pricingEnabled: Boolean = true,
    /**
     * The user's region, resolved from device location when permitted. When null, pricing is
     * generic (a neutral currency) rather than localized to a country.
     */
    val region: Region? = null,
)
