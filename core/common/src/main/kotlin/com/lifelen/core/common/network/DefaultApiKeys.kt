package com.lifelen.core.common.network

/**
 * Build-time default API keys, injected so the app works out of the box. These are used only as a
 * fallback by [ApiKeyProvider] when the user hasn't entered their own key — they are never written
 * to storage and never shown in the Settings UI.
 */
data class DefaultApiKeys(
    val dashScopeKey: String = "",
    val searchKey: String = "",
)
