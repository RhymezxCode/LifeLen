package com.lifelen.core.data.repository

import com.lifelen.core.data.model.ScanOptions
import com.lifelen.core.datastore.UserPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Settings exposed to the UI, decoupled from the datastore representation. */
data class AppSettings(
    val dashScopeApiKey: String = "",
    val searchApiKey: String = "",
    val pricingEnabled: Boolean = true,
    val darkTheme: Boolean? = null,
) {
    val hasVisionKey: Boolean get() = dashScopeApiKey.isNotBlank()
    val hasSearchKey: Boolean get() = searchApiKey.isNotBlank()
}

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun scanOptions(): ScanOptions
    suspend fun setDashScopeApiKey(value: String)
    suspend fun setSearchApiKey(value: String)
    suspend fun setPricingEnabled(enabled: Boolean)
    suspend fun setDarkTheme(enabled: Boolean?)
    /** Seeds keys from build-time defaults only when the user hasn't set their own. */
    suspend fun seedDefaultsIfEmpty(dashScopeKey: String, searchKey: String)
}

class DefaultSettingsRepository @Inject constructor(
    private val dataSource: UserPreferencesDataSource,
) : SettingsRepository {

    override val settings: Flow<AppSettings> = dataSource.preferences.map {
        AppSettings(
            dashScopeApiKey = it.dashScopeApiKey,
            searchApiKey = it.searchApiKey,
            pricingEnabled = it.pricingEnabled,
            darkTheme = it.darkTheme,
        )
    }

    override suspend fun scanOptions(): ScanOptions =
        ScanOptions(pricingEnabled = dataSource.preferences.first().pricingEnabled)

    override suspend fun setDashScopeApiKey(value: String) = dataSource.setDashScopeApiKey(value)

    override suspend fun setSearchApiKey(value: String) = dataSource.setSearchApiKey(value)

    override suspend fun setPricingEnabled(enabled: Boolean) = dataSource.setPricingEnabled(enabled)

    override suspend fun setDarkTheme(enabled: Boolean?) = dataSource.setDarkTheme(enabled)

    override suspend fun seedDefaultsIfEmpty(dashScopeKey: String, searchKey: String) {
        val current = dataSource.preferences.first()
        if (current.dashScopeApiKey.isBlank() && dashScopeKey.isNotBlank()) {
            dataSource.setDashScopeApiKey(dashScopeKey)
        }
        if (current.searchApiKey.isBlank() && searchKey.isNotBlank()) {
            dataSource.setSearchApiKey(searchKey)
        }
    }
}
