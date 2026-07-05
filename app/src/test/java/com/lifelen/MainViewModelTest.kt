package com.lifelen

import app.cash.turbine.test
import com.lifelen.core.data.model.ScanOptions
import com.lifelen.core.data.repository.AppSettings
import com.lifelen.core.data.repository.SettingsRepository
import com.lifelen.core.model.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val d: TestDispatcher = UnconfinedTestDispatcher(),
) : org.junit.rules.TestWatcher() {
    override fun starting(x: org.junit.runner.Description) {
        Dispatchers.setMain(d)
    }

    override fun finished(x: org.junit.runner.Description) {
        Dispatchers.resetMain()
    }
}

private class FakeSettingsRepository(
    initial: AppSettings,
) : SettingsRepository {
    val flow = MutableStateFlow(initial)
    override val settings: Flow<AppSettings> = flow
    override suspend fun scanOptions(): ScanOptions = ScanOptions()
    override suspend fun setDashScopeApiKey(value: String) = Unit
    override suspend fun setSearchApiKey(value: String) = Unit
    override suspend fun setPricingEnabled(enabled: Boolean) = Unit
    override suspend fun setThemeMode(mode: ThemeMode) = Unit
    override suspend fun setHapticsEnabled(enabled: Boolean) = Unit
    override suspend fun setAutoSaveScans(enabled: Boolean) = Unit
    override suspend fun setAutoScan(enabled: Boolean) = Unit
    override suspend fun setRememberKeys(remember: Boolean) = Unit
    override suspend fun seedDefaultsIfEmpty(dashScopeKey: String, searchKey: String) = Unit
}

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `themeMode reflects the settings repository`() = runTest {
        val repo = FakeSettingsRepository(AppSettings(themeMode = ThemeMode.DARK))
        val viewModel = MainViewModel(repo)

        viewModel.themeMode.test {
            var mode = awaitItem()
            while (mode != ThemeMode.DARK) mode = awaitItem()
            assertEquals(ThemeMode.DARK, mode)
            cancelAndConsumeRemainingEvents()
        }
    }
}
