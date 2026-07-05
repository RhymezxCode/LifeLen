package com.lifelen.core.datastore

import io.github.rhymezxcode.simplestore.DatastorePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for user preferences, backed by the SimpleStore DataStore wrapper
 * (RhymezxCode/SimpleStore). Pricing is persisted as an inverted "disabled" flag so the unset
 * default (false) means pricing is ON.
 */
@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val store: DatastorePreference,
) {
    val preferences: Flow<UserPreferences> = combine(
        store.getStringFromStore(Keys.DASHSCOPE_KEY),
        store.getStringFromStore(Keys.SEARCH_KEY),
        store.getBooleanFromStore(Keys.PRICING_DISABLED),
    ) { dashScopeKey, searchKey, pricingDisabled ->
        UserPreferences(
            dashScopeApiKey = dashScopeKey,
            searchApiKey = searchKey,
            pricingEnabled = !pricingDisabled,
            darkTheme = null, // v1 is dark-only.
        )
    }

    suspend fun setDashScopeApiKey(value: String) =
        store.saveStringToStore(Keys.DASHSCOPE_KEY, value.trim())

    suspend fun setSearchApiKey(value: String) =
        store.saveStringToStore(Keys.SEARCH_KEY, value.trim())

    suspend fun setPricingEnabled(enabled: Boolean) =
        store.saveBooleanToStore(Keys.PRICING_DISABLED, !enabled)

    /** No-op in v1 (dark theme only); kept for API compatibility. */
    @Suppress("UNUSED_PARAMETER")
    suspend fun setDarkTheme(enabled: Boolean?) = Unit

    private object Keys {
        const val DASHSCOPE_KEY = "dashscope_api_key"
        const val SEARCH_KEY = "search_api_key"
        const val PRICING_DISABLED = "pricing_disabled"
    }
}
