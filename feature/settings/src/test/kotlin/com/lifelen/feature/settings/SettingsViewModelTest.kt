package com.lifelen.feature.settings

import app.cash.turbine.test
import com.lifelen.core.data.repository.AppSettings
import com.lifelen.core.model.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Pure JVM test — [SettingsViewModel] has no Context-bound dependency, so no Robolectric runner is
 * needed. It lives in the module for locality with the settings screen test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `uiState reflects the settings repository`() = runTest {
        val settings = FakeSettingsRepository(
            AppSettings(dashScopeApiKey = "k", themeMode = ThemeMode.DARK, pricingEnabled = false),
        )
        val vm = SettingsViewModel(settings, FakeHistoryRepository())

        vm.uiState.test {
            var state = awaitItem()
            while (state.themeMode != ThemeMode.DARK) state = awaitItem()
            assertEquals("k", state.dashScopeApiKey)
            assertFalse(state.pricingEnabled)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `saveKeys delegates both keys to the repository`() = runTest {
        val settings = FakeSettingsRepository()
        val vm = SettingsViewModel(settings, FakeHistoryRepository())

        vm.saveKeys("a", "b")

        assertEquals(listOf("a"), settings.dashScopeKeys)
        assertEquals(listOf("b"), settings.searchKeys)
    }

    @Test
    fun `toggle setters delegate to the repository`() = runTest {
        val settings = FakeSettingsRepository()
        val vm = SettingsViewModel(settings, FakeHistoryRepository())

        vm.setThemeMode(ThemeMode.DARK)
        vm.setPricingEnabled(false)
        vm.setHapticsEnabled(false)
        vm.setAutoSaveScans(true)
        vm.setRememberKeys(false)

        assertEquals(ThemeMode.DARK, settings.theme)
        assertEquals(false, settings.pricing)
        assertEquals(false, settings.haptics)
        assertEquals(true, settings.autoSave)
        assertEquals(false, settings.remember)
    }

    @Test
    fun `clearLibrary delegates to the history repository`() = runTest {
        val history = FakeHistoryRepository()
        val vm = SettingsViewModel(FakeSettingsRepository(), history)

        vm.clearLibrary()

        assertTrue(history.clearAllCalled)
    }
}
