package com.lifelen.feature.prices

import com.lifelen.core.common.result.DataResult
import com.lifelen.core.data.model.ScanOptions
import com.lifelen.core.data.repository.AppSettings
import com.lifelen.core.data.repository.HistoryRepository
import com.lifelen.core.data.repository.ScanRepository
import com.lifelen.core.data.repository.SettingsRepository
import com.lifelen.core.data.session.CaptureDraft
import com.lifelen.core.model.BuyOption
import com.lifelen.core.model.Identification
import com.lifelen.core.model.PriceCondition
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
        scans.value = emptyList()
    }
}

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
    override suspend fun setRememberKeys(remember: Boolean) = settingsFlow.update { it.copy(rememberKeys = remember) }
    override suspend fun seedDefaultsIfEmpty(dashScopeKey: String, searchKey: String) = Unit
}

/** Priced scan with both NEW and RENEWED buy options; lowest ("$699") is a summary figure only. */
fun samplePrice(): PriceInfo = PriceInfo(
    currency = "$",
    lowPrice = 699.0,
    highPrice = 999.0,
    average = 850.0,
    source = "Google Shopping",
    options = listOf(
        BuyOption("Amazon", 849.0, "$", "https://a", true, PriceCondition.NEW, "Free shipping"),
        BuyOption("Best Buy", 899.0, "$", "https://b", true, PriceCondition.NEW, "Pickup today"),
        BuyOption("Swappa", 749.0, "$", "https://s", true, PriceCondition.RENEWED, "1 yr warranty"),
    ),
)

fun sampleElectronics(id: String = "e1"): Scan = Scan(
    id = id,
    imagePath = "/e.jpg",
    identification = Identification(
        title = "MacBook Air 13-inch",
        category = ScanCategory.ELECTRONICS,
        summary = "A thin-and-light laptop.",
        confidence = 0.94f,
    ),
    price = samplePrice(),
    createdAt = 1_700_000_000_000L,
)
