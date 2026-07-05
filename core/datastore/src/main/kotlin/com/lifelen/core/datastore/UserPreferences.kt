package com.lifelen.core.datastore

/** User-controlled settings, including runtime-entered API keys. */
data class UserPreferences(
    val dashScopeApiKey: String = "",
    val searchApiKey: String = "",
    val pricingEnabled: Boolean = true,
    /** null = follow the system setting. */
    val darkTheme: Boolean? = null,
)
