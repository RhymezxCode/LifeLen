package com.lifelen.feature.results

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.lifelen.core.common.result.DataResult
import com.lifelen.core.data.image.ImageStore
import com.lifelen.core.data.session.ScanSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Robolectric-backed: [ResultsViewModel] constructs a real [ScanSession] (Context-bound [ImageStore]).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ResultsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private fun session() = ScanSession(ImageStore(context))

    private fun vm(
        scanRepo: FakeScanRepository,
        history: FakeHistoryRepository = FakeHistoryRepository(),
        scanSession: ScanSession = session(),
        scanId: String = "current",
    ) = ResultsViewModel(
        scanRepository = scanRepo,
        historyRepository = history,
        settingsRepository = FakeSettingsRepository(),
        scanSession = scanSession,
        savedStateHandle = SavedStateHandle(mapOf("scanId" to scanId)),
    )

    @Test
    fun `fresh capture success becomes Ready and not saved`() = runTest {
        val scan = sampleElectronics()
        val scanSession = session().apply { beginCapture(byteArrayOf(1, 2, 3)) }
        val viewModel = vm(FakeScanRepository(identifyResult = DataResult.Success(scan)), scanSession = scanSession)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state !is ResultsUiState.Ready) state = awaitItem()
            assertFalse(state.saved)
            assertEquals(scan, state.scan)
            cancelAndConsumeRemainingEvents()
        }
        // A successful fresh identification is cached in the session for the prices screen.
        assertEquals(scan, scanSession.currentResult())
    }

    @Test
    fun `fresh capture failure becomes Failed`() = runTest {
        val scanSession = session().apply { beginCapture(byteArrayOf(9)) }
        val viewModel = vm(
            FakeScanRepository(identifyResult = DataResult.Error(RuntimeException("boom"))),
            scanSession = scanSession,
        )

        viewModel.uiState.test {
            var state = awaitItem()
            while (state !is ResultsUiState.Failed) state = awaitItem()
            assertEquals("boom", state.message)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `saved detail loads the persisted scan as Ready and saved`() = runTest {
        val saved = sampleElectronics(id = "abc")
        val viewModel = vm(
            FakeScanRepository(),
            history = FakeHistoryRepository(listOf(saved)),
            scanId = "abc",
        )

        viewModel.uiState.test {
            var state = awaitItem()
            while (state !is ResultsUiState.Ready) state = awaitItem()
            assertTrue(state.saved)
            assertEquals("abc", state.scan.id)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `saved detail with a missing id becomes NotFound`() = runTest {
        val viewModel = vm(FakeScanRepository(), history = FakeHistoryRepository(emptyList()), scanId = "missing")

        viewModel.uiState.test {
            var state = awaitItem()
            while (state !is ResultsUiState.NotFound) state = awaitItem()
            assertEquals(ResultsUiState.NotFound, state)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `save persists the scan, flips saved, and emits Saved`() = runTest {
        val scan = sampleElectronics()
        val scanSession = session().apply { beginCapture(byteArrayOf(1)) }
        val scanRepo = FakeScanRepository(identifyResult = DataResult.Success(scan))
        val viewModel = vm(scanRepo, scanSession = scanSession)

        // Reach Ready first.
        viewModel.uiState.test {
            var state = awaitItem()
            while (state !is ResultsUiState.Ready) state = awaitItem()
            cancelAndConsumeRemainingEvents()
        }

        viewModel.events.test {
            viewModel.save()
            assertEquals(ResultEvent.Saved, awaitItem())
            cancelAndConsumeRemainingEvents()
        }

        assertTrue(scanRepo.savedScans.any { it.id == scan.id })
        assertTrue((viewModel.uiState.value as ResultsUiState.Ready).saved)
    }

    @Test
    fun `refresh swaps in the refreshed scan`() = runTest {
        val scan = sampleElectronics()
        val refreshed = scan.copy(price = scan.price?.copy(lowPrice = 799.0))
        val scanSession = session().apply { beginCapture(byteArrayOf(1)) }
        val scanRepo = FakeScanRepository(
            identifyResult = DataResult.Success(scan),
            refreshResult = DataResult.Success(refreshed),
        )
        val viewModel = vm(scanRepo, scanSession = scanSession)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state !is ResultsUiState.Ready) state = awaitItem()
            viewModel.refresh()
            while (state !is ResultsUiState.Ready || state.scan.price?.lowPrice != 799.0) state = awaitItem()
            assertEquals(refreshed, state.scan)
            cancelAndConsumeRemainingEvents()
        }
        assertTrue(scanRepo.refreshCalls >= 1)
    }

    @Test
    fun `setPortion clamps to the 0_5 to 4 range`() = runTest {
        val scan = sampleFood()
        val scanSession = session().apply { beginCapture(byteArrayOf(1)) }
        val viewModel = vm(FakeScanRepository(identifyResult = DataResult.Success(scan)), scanSession = scanSession)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state !is ResultsUiState.Ready) state = awaitItem()

            viewModel.setPortion(9f)
            while (state !is ResultsUiState.Ready || state.portionFactor != 4f) state = awaitItem()
            assertEquals(4f, (state as ResultsUiState.Ready).portionFactor)

            viewModel.setPortion(0.1f)
            while (state !is ResultsUiState.Ready || state.portionFactor != 0.5f) state = awaitItem()
            assertEquals(0.5f, (state as ResultsUiState.Ready).portionFactor)

            cancelAndConsumeRemainingEvents()
        }
    }
}
