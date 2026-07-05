package com.lifelen.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Single source of truth for user preferences, backed by Preferences DataStore. */
@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val preferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            dashScopeApiKey = prefs[Keys.DASHSCOPE_KEY].orEmpty(),
            searchApiKey = prefs[Keys.SEARCH_KEY].orEmpty(),
            pricingEnabled = prefs[Keys.PRICING_ENABLED] ?: true,
            darkTheme = prefs[Keys.DARK_THEME],
        )
    }

    suspend fun setDashScopeApiKey(value: String) =
        dataStore.editValue { it[Keys.DASHSCOPE_KEY] = value.trim() }

    suspend fun setSearchApiKey(value: String) =
        dataStore.editValue { it[Keys.SEARCH_KEY] = value.trim() }

    suspend fun setPricingEnabled(enabled: Boolean) =
        dataStore.editValue { it[Keys.PRICING_ENABLED] = enabled }

    suspend fun setDarkTheme(enabled: Boolean?) =
        dataStore.editValue { prefs ->
            if (enabled == null) prefs.remove(Keys.DARK_THEME) else prefs[Keys.DARK_THEME] = enabled
        }

    private suspend fun DataStore<Preferences>.editValue(
        transform: (androidx.datastore.preferences.core.MutablePreferences) -> Unit,
    ) {
        edit { transform(it) }
    }

    private object Keys {
        val DASHSCOPE_KEY = stringPreferencesKey("dashscope_api_key")
        val SEARCH_KEY = stringPreferencesKey("search_api_key")
        val PRICING_ENABLED = booleanPreferencesKey("pricing_enabled")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }
}
