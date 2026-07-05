package com.lifelen.core.datastore

/** User-controlled settings, including runtime-entered API keys. [themeMode] is a raw string. */
data class UserPreferences(
    val dashScopeApiKey: String = "",
    val searchApiKey: String = "",
    val pricingEnabled: Boolean = true,
    val themeMode: String = "system",
    val hapticsEnabled: Boolean = true,
    val autoSaveScans: Boolean = false,
    val rememberKeys: Boolean = true,
)
