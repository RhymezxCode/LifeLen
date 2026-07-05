package com.lifelen.feature.scanner

import com.lifelen.core.data.model.ScanOptions
import com.lifelen.core.data.repository.AppSettings
import com.lifelen.core.data.repository.HistoryRepository
import com.lifelen.core.data.repository.SettingsRepository
import com.lifelen.core.model.Identification
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.model.Scan
import com.lifelen.core.model.ScanCategory
import com.lifelen.core.model.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** Swaps `Dispatchers.Main` for a test dispatcher for the duration of a test. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

/** In-memory [HistoryRepository] backed by a [MutableStateFlow]. */
class FakeHistoryRepository(initial: List<Scan> = emptyList()) : HistoryRepository {
    val scans = MutableStateFlow(initial)
    var clearAllCalled = false

    override fun observeHistory(): Flow<List<Scan>> = scans
    override fun observeFavorites(): Flow<List<Scan>> = scans.map { list -> list.filter { it.isFavorite } }
    override fun search(query: String): Flow<List<Scan>> =
        scans.map { list -> list.filter { it.title.contains(query, ignoreCase = true) } }

    override suspend fun getScan(id: String): Scan? = scans.value.firstOrNull { it.id == id }
    override suspend fun toggleFavorite(id: String, isFavorite: Boolean) = Unit
    override suspend fun delete(id: String) {
        scans.value = scans.value.filterNot { it.id == id }
    }

    override suspend fun clearAll() {
        clearAllCalled = true
        scans.value = emptyList()
    }
}

/** In-memory [SettingsRepository]; setters record their arguments. */
class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {
    val settingsFlow = MutableStateFlow(initial)
    override val settings: Flow<AppSettings> = settingsFlow

    val dashScopeKeys = mutableListOf<String>()
    val searchKeys = mutableListOf<String>()
    var pricing: Boolean? = null
    var theme: ThemeMode? = null
    var haptics: Boolean? = null
    var autoSave: Boolean? = null
    var remember: Boolean? = null

    override suspend fun scanOptions(): ScanOptions =
        ScanOptions(pricingEnabled = settingsFlow.value.pricingEnabled)

    override suspend fun setDashScopeApiKey(value: String) {
        dashScopeKeys += value
        settingsFlow.update { it.copy(dashScopeApiKey = value) }
    }

    override suspend fun setSearchApiKey(value: String) {
        searchKeys += value
        settingsFlow.update { it.copy(searchApiKey = value) }
    }

    override suspend fun setPricingEnabled(enabled: Boolean) {
        pricing = enabled
        settingsFlow.update { it.copy(pricingEnabled = enabled) }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        theme = mode
        settingsFlow.update { it.copy(themeMode = mode) }
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        haptics = enabled
        settingsFlow.update { it.copy(hapticsEnabled = enabled) }
    }

    override suspend fun setAutoSaveScans(enabled: Boolean) {
        autoSave = enabled
        settingsFlow.update { it.copy(autoSaveScans = enabled) }
    }

    override suspend fun setRememberKeys(remember: Boolean) {
        this.remember = remember
        settingsFlow.update { it.copy(rememberKeys = remember) }
    }

    override suspend fun seedDefaultsIfEmpty(dashScopeKey: String, searchKey: String) = Unit
}

/** A priced electronics [Scan] with four spec attributes (values include "M2"). */
fun sampleElectronics(id: String = "e1"): Scan = Scan(
    id = id,
    imagePath = "/e.jpg",
    identification = Identification(
        title = "MacBook Air 13-inch",
        category = ScanCategory.ELECTRONICS,
        summary = "A thin-and-light laptop.",
        confidence = 0.94f,
        attributes = linkedMapOf(
            "Chip" to "M2",
            "Memory" to "8 GB",
            "Storage" to "256 GB",
            "Display" to "13.6\"",
        ),
    ),
    price = PriceInfo(
        currency = "$",
        lowPrice = 849.0,
        highPrice = 999.0,
        average = 967.0,
    ),
    createdAt = 1_700_000_000_000L,
)
