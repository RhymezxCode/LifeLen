package com.lifelen.core.datastore

import io.github.rhymezxcode.simplestore.DatastorePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for user preferences, backed by the SimpleStore DataStore wrapper
 * (RhymezxCode/SimpleStore). Booleans that default to ON (pricing, haptics, remember-keys) are
 * persisted as inverted "disabled/forgotten" flags so the unset default is the ON state.
 */
@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val store: DatastorePreference,
) {
    val preferences: Flow<UserPreferences> = combine(
        store.getStringFromStore(Keys.DASHSCOPE_KEY),
        store.getStringFromStore(Keys.SEARCH_KEY),
        store.getBooleanFromStore(Keys.PRICING_DISABLED),
        store.getStringFromStore(Keys.THEME_MODE),
        store.getBooleanFromStore(Keys.HAPTICS_DISABLED),
        store.getBooleanFromStore(Keys.AUTO_SAVE),
        store.getBooleanFromStore(Keys.KEYS_FORGOTTEN),
    ) { values ->
        UserPreferences(
            dashScopeApiKey = values[0] as String,
            searchApiKey = values[1] as String,
            pricingEnabled = !(values[2] as Boolean),
            themeMode = (values[3] as String).ifBlank { "system" },
            hapticsEnabled = !(values[4] as Boolean),
            autoSaveScans = values[5] as Boolean,
            rememberKeys = !(values[6] as Boolean),
        )
    }

    suspend fun setDashScopeApiKey(value: String) =
        store.saveStringToStore(Keys.DASHSCOPE_KEY, value.trim())

    suspend fun setSearchApiKey(value: String) =
        store.saveStringToStore(Keys.SEARCH_KEY, value.trim())

    suspend fun setPricingEnabled(enabled: Boolean) =
        store.saveBooleanToStore(Keys.PRICING_DISABLED, !enabled)

    suspend fun setThemeMode(value: String) =
        store.saveStringToStore(Keys.THEME_MODE, value)

    suspend fun setHapticsEnabled(enabled: Boolean) =
        store.saveBooleanToStore(Keys.HAPTICS_DISABLED, !enabled)

    suspend fun setAutoSaveScans(enabled: Boolean) =
        store.saveBooleanToStore(Keys.AUTO_SAVE, enabled)

    /** When remember is turned off, the stored keys are wiped so they don't linger on device. */
    suspend fun setRememberKeys(remember: Boolean) {
        store.saveBooleanToStore(Keys.KEYS_FORGOTTEN, !remember)
        if (!remember) {
            store.saveStringToStore(Keys.DASHSCOPE_KEY, "")
            store.saveStringToStore(Keys.SEARCH_KEY, "")
        }
    }

    private object Keys {
        const val DASHSCOPE_KEY = "dashscope_api_key"
        const val SEARCH_KEY = "search_api_key"
        const val PRICING_DISABLED = "pricing_disabled"
        const val THEME_MODE = "theme_mode"
        const val HAPTICS_DISABLED = "haptics_disabled"
        const val AUTO_SAVE = "auto_save_scans"
        const val KEYS_FORGOTTEN = "keys_forgotten"
    }
}
