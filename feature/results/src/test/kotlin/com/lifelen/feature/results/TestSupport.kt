package com.lifelen.feature.results

import com.lifelen.core.common.result.DataResult
import com.lifelen.core.data.connectivity.NetworkMonitor
import com.lifelen.core.data.model.ScanOptions
import com.lifelen.core.data.repository.AppSettings
import com.lifelen.core.data.repository.HistoryRepository
import com.lifelen.core.data.repository.ScanRepository
import com.lifelen.core.data.repository.SettingsRepository
import com.lifelen.core.data.session.CaptureDraft
import com.lifelen.core.model.Identification
import com.lifelen.core.model.NutritionInfo
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

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

class FakeHistoryRepository(initial: List<Scan> = emptyList()) : HistoryRepository {
    val scans = MutableStateFlow(initial)
    val favoriteCalls = mutableListOf<Pair<String, Boolean>>()
    val deletedIds = mutableListOf<String>()

    override fun observeHistory(): Flow<List<Scan>> = scans
    override fun observeFavorites(): Flow<List<Scan>> = scans.map { list -> list.filter { it.isFavorite } }
    override fun search(query: String): Flow<List<Scan>> =
        scans.map { list -> list.filter { it.title.contains(query, ignoreCase = true) } }

    override suspend fun getScan(id: String): Scan? = scans.value.firstOrNull { it.id == id }
    override suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        favoriteCalls += id to isFavorite
        scans.value = scans.value.map { if (it.id == id) it.copy(isFavorite = isFavorite) else it }
    }

    override suspend fun delete(id: String) {
        deletedIds += id
        scans.value = scans.value.filterNot { it.id == id }
    }

    override suspend fun clearAll() {
        scans.value = emptyList()
    }
}

/** Toggleable connectivity for the offline-fallback tests. */
class FakeNetworkMonitor(var online: Boolean = true) : NetworkMonitor {
    override fun isOnline(): Boolean = online
}

/** A document [Scan] whose transcribed text lives in the "Text" attribute. */
fun sampleDocument(id: String = "d1"): Scan = Scan(
    id = id,
    imagePath = "/d.jpg",
    identification = Identification(
        title = "Handwritten note",
        category = ScanCategory.DOCUMENT,
        summary = "A handwritten note.",
        confidence = 0.7f,
        attributes = linkedMapOf("Text" to "Meeting at 3pm, bring the quarterly report."),
    ),
    createdAt = 1_700_000_000_000L,
)

/** Configurable [ScanRepository]; records [save] calls and returns canned results. */
class FakeScanRepository(
    var identifyResult: DataResult<Scan> = DataResult.Error(IllegalStateException("not configured")),
    var refreshResult: DataResult<Scan>? = null,
) : ScanRepository {
    val savedScans = mutableListOf<Scan>()
    var refreshCalls = 0

    override suspend fun identify(draft: CaptureDraft, options: ScanOptions): DataResult<Scan> = identifyResult

    override suspend fun save(scan: Scan) {
        savedScans += scan
    }

    override suspend fun refreshPrice(scan: Scan, options: ScanOptions): DataResult<Scan> {
        refreshCalls++
        return refreshResult ?: DataResult.Success(scan)
    }

    var askResult: DataResult<String> = DataResult.Success("It runs Qwen-VL on the edge.")
    override suspend fun ask(scan: Scan, question: String): DataResult<String> = askResult
}

class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {
    val settingsFlow = MutableStateFlow(initial)
    override val settings: Flow<AppSettings> = settingsFlow

    override suspend fun scanOptions(): ScanOptions =
        ScanOptions(pricingEnabled = settingsFlow.value.pricingEnabled)

    override suspend fun setDashScopeApiKey(value: String) = settingsFlow.update { it.copy(dashScopeApiKey = value) }
    override suspend fun setSearchApiKey(value: String) = settingsFlow.update { it.copy(searchApiKey = value) }
    override suspend fun setPricingEnabled(enabled: Boolean) = settingsFlow.update { it.copy(pricingEnabled = enabled) }
    override suspend fun setThemeMode(mode: ThemeMode) = settingsFlow.update { it.copy(themeMode = mode) }
    override suspend fun setHapticsEnabled(enabled: Boolean) = settingsFlow.update { it.copy(hapticsEnabled = enabled) }
    override suspend fun setAutoSaveScans(enabled: Boolean) = settingsFlow.update { it.copy(autoSaveScans = enabled) }
    override suspend fun setAutoScan(enabled: Boolean) = settingsFlow.update { it.copy(autoScan = enabled) }
    override suspend fun setRememberKeys(remember: Boolean) = settingsFlow.update { it.copy(rememberKeys = remember) }
    override suspend fun seedDefaultsIfEmpty(dashScopeKey: String, searchKey: String) = Unit
}

/** A priced electronics [Scan]; stat values include "M2" and the title deliberately does not. */
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

/** A food [Scan] carrying nutrition so the result sheet renders the FoodResultBody ("kcal"). */
fun sampleFood(id: String = "f1"): Scan = Scan(
    id = id,
    imagePath = "/f.jpg",
    identification = Identification(
        title = "Jollof rice with grilled chicken",
        category = ScanCategory.FOOD,
        summary = "",
        confidence = 0.8f,
    ),
    nutrition = NutritionInfo(
        servingSize = "1 plate · ~350 g",
        calories = 540,
        protein = 28.0,
        carbs = 62.0,
        fat = 19.0,
        fiber = 4.0,
        sugars = 6.0,
        sodium = 890,
        ingredients = listOf("rice", "chicken", "sauce"),
    ),
    createdAt = 1_700_000_000_000L,
)
