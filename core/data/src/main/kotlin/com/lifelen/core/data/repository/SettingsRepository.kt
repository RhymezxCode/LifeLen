package com.lifelen.core.data.repository

import com.lifelen.core.common.location.RegionProvider
import com.lifelen.core.data.model.ScanOptions
import com.lifelen.core.datastore.UserPreferencesDataSource
import com.lifelen.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Settings exposed to the UI, decoupled from the datastore representation. */
data class AppSettings(
    val dashScopeApiKey: String = "",
    val searchApiKey: String = "",
    val pricingEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hapticsEnabled: Boolean = true,
    val autoSaveScans: Boolean = false,
    val rememberKeys: Boolean = true,
    /** When on, the camera auto-identifies a steady subject instead of waiting for the shutter. */
    val autoScan: Boolean = false,
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
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setAutoSaveScans(enabled: Boolean)
    suspend fun setAutoScan(enabled: Boolean)
    suspend fun setRememberKeys(remember: Boolean)

    /**
     * Migration: clears any stored key that merely echoes the build-time default, so the bundled
     * default no longer appears in Settings. The app still works via the [ApiKeyProvider] fallback.
     */
    suspend fun reconcileDefaultKeys(dashScopeKey: String, searchKey: String)
}

class DefaultSettingsRepository @Inject constructor(
    private val dataSource: UserPreferencesDataSource,
    private val regionProvider: RegionProvider,
) : SettingsRepository {

    override val settings: Flow<AppSettings> = dataSource.preferences.map {
        AppSettings(
            dashScopeApiKey = it.dashScopeApiKey,
            searchApiKey = it.searchApiKey,
            pricingEnabled = it.pricingEnabled,
            themeMode = ThemeMode.fromName(it.themeMode),
            hapticsEnabled = it.hapticsEnabled,
            autoSaveScans = it.autoSaveScans,
            rememberKeys = it.rememberKeys,
            autoScan = it.autoScan,
        )
    }

    override suspend fun scanOptions(): ScanOptions =
        ScanOptions(
            pricingEnabled = dataSource.preferences.first().pricingEnabled,
            // Null when location permission isn't granted → generic (non-localized) pricing.
            region = regionProvider.currentRegion(),
        )

    override suspend fun setDashScopeApiKey(value: String) = dataSource.setDashScopeApiKey(value)

    override suspend fun setSearchApiKey(value: String) = dataSource.setSearchApiKey(value)

    override suspend fun setPricingEnabled(enabled: Boolean) = dataSource.setPricingEnabled(enabled)

    override suspend fun setThemeMode(mode: ThemeMode) = dataSource.setThemeMode(mode.name.lowercase())

    override suspend fun setHapticsEnabled(enabled: Boolean) = dataSource.setHapticsEnabled(enabled)

    override suspend fun setAutoSaveScans(enabled: Boolean) = dataSource.setAutoSaveScans(enabled)

    override suspend fun setAutoScan(enabled: Boolean) = dataSource.setAutoScan(enabled)

    override suspend fun setRememberKeys(remember: Boolean) = dataSource.setRememberKeys(remember)

    override suspend fun reconcileDefaultKeys(dashScopeKey: String, searchKey: String) {
        val current = dataSource.preferences.first()
        // Older builds seeded the default key into storage, which then showed in Settings. Drop it —
        // the ApiKeyProvider now supplies the default as a fallback without persisting it.
        if (dashScopeKey.isNotBlank() && current.dashScopeApiKey == dashScopeKey) {
            dataSource.setDashScopeApiKey("")
        }
        if (searchKey.isNotBlank() && current.searchApiKey == searchKey) {
            dataSource.setSearchApiKey("")
        }
    }
}
