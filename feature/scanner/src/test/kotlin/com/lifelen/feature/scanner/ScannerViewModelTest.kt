package com.lifelen.feature.scanner

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.lifelen.core.data.image.ImageStore
import com.lifelen.core.data.repository.AppSettings
import com.lifelen.core.data.session.ScanSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Robolectric-backed: [ScannerViewModel] transitively constructs a real [ScanSession], which needs
 * a Context-bound [ImageStore] to persist captures.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ScannerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private fun session() = ScanSession(ImageStore(context))

    @Test
    fun `init mirrors library and vision-key state`() = runTest {
        val history = FakeHistoryRepository(listOf(sampleElectronics(id = "e1")))
        val settings = FakeSettingsRepository(AppSettings(dashScopeApiKey = "vision-key"))
        val vm = ScannerViewModel(session(), history, settings)

        vm.uiState.test {
            var state = awaitItem()
            while (state.libraryCount != 1 || !state.hasVisionKey) state = awaitItem()
            assertEquals(1, state.libraryCount)
            assertEquals("/e.jpg", state.lastThumbPath)
            assertTrue(state.hasVisionKey)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `no vision key is reflected`() = runTest {
        val settings = FakeSettingsRepository(AppSettings(dashScopeApiKey = ""))
        val vm = ScannerViewModel(session(), FakeHistoryRepository(), settings)

        vm.uiState.test {
            var state = awaitItem()
            while (state.hasVisionKey) state = awaitItem()
            assertFalse(state.hasVisionKey)
            assertEquals(0, state.libraryCount)
            assertNull(state.lastThumbPath)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `onCaptured stores a draft and emits one navigation event`() = runTest {
        val scanSession = session()
        val vm = ScannerViewModel(scanSession, FakeHistoryRepository(), FakeSettingsRepository())
        assertNull(scanSession.currentDraft())

        vm.events.test {
            vm.onCaptured(byteArrayOf(1, 2, 3))
            awaitItem() // exactly one Unit signal
            expectNoEvents()
            cancelAndConsumeRemainingEvents()
        }

        assertNotNull(scanSession.currentDraft())
        assertFalse(vm.uiState.value.isCapturing) // capture finished
    }

    @Test
    fun `selectMode updates the selected mode`() = runTest {
        val vm = ScannerViewModel(session(), FakeHistoryRepository(), FakeSettingsRepository())
        assertEquals("Auto", vm.uiState.value.selectedMode)
        vm.selectMode("Food")
        assertEquals("Food", vm.uiState.value.selectedMode)
    }

    @Test
    fun `capture error is set then cleared`() = runTest {
        val vm = ScannerViewModel(session(), FakeHistoryRepository(), FakeSettingsRepository())
        vm.onCaptureError("x")
        assertEquals("x", vm.uiState.value.error)
        vm.dismissError()
        assertNull(vm.uiState.value.error)
    }
}
