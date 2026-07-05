package com.lifelen.feature.prices

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.lifelen.core.common.result.DataResult
import com.lifelen.core.data.image.ImageStore
import com.lifelen.core.data.session.ScanSession
import com.lifelen.core.model.PriceCondition
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
 * Robolectric-backed: [PricesViewModel] constructs a real [ScanSession] (Context-bound [ImageStore]).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PricesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private fun session() = ScanSession(ImageStore(context))

    private fun vm(
        history: FakeHistoryRepository = FakeHistoryRepository(),
        scanRepo: FakeScanRepository = FakeScanRepository(),
        scanSession: ScanSession = session(),
        scanId: String? = null,
    ) = PricesViewModel(
        historyRepository = history,
        scanRepository = scanRepo,
        settingsRepository = FakeSettingsRepository(),
        scanSession = scanSession,
        savedStateHandle = SavedStateHandle(if (scanId == null) emptyMap() else mapOf("scanId" to scanId)),
    )

    @Test
    fun `resolves a priced scan from history`() = runTest {
        val scan = sampleElectronics(id = "abc")
        val viewModel = vm(history = FakeHistoryRepository(listOf(scan)), scanId = "abc")

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.price == null) state = awaitItem()
            assertEquals(scan.price, state.price)
            assertEquals(scan.title, state.title)
            assertFalse(state.notFound)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `falls back to the in-flight scan in the session`() = runTest {
        val scan = sampleElectronics()
        val scanSession = session().also { it.setResult(scan) }
        val viewModel = vm(scanSession = scanSession, scanId = null)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.price == null) state = awaitItem()
            assertEquals(scan.price, state.price)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `not found when neither history nor session has the scan`() = runTest {
        val viewModel = vm(history = FakeHistoryRepository(emptyList()), scanId = "missing")

        viewModel.uiState.test {
            var state = awaitItem()
            while (!state.notFound) state = awaitItem()
            assertTrue(state.notFound)
            assertEquals(null, state.price)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `selectCondition updates the selected condition`() = runTest {
        val viewModel = vm()
        assertEquals(PriceCondition.NEW, viewModel.uiState.value.selectedCondition)
        viewModel.selectCondition(PriceCondition.RENEWED)
        assertEquals(PriceCondition.RENEWED, viewModel.uiState.value.selectedCondition)
    }

    @Test
    fun `refresh re-fetches pricing and swaps in the new price`() = runTest {
        val scan = sampleElectronics(id = "abc")
        val refreshed = scan.copy(price = scan.price?.copy(lowPrice = 599.0))
        val scanRepo = FakeScanRepository(refreshResult = DataResult.Success(refreshed))
        val viewModel = vm(history = FakeHistoryRepository(listOf(scan)), scanRepo = scanRepo, scanId = "abc")

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.price == null) state = awaitItem()
            viewModel.refresh()
            while (state.price?.lowPrice != 599.0) state = awaitItem()
            assertEquals(599.0, state.price?.lowPrice)
            assertFalse(state.isRefreshing)
            cancelAndConsumeRemainingEvents()
        }
        assertTrue(scanRepo.refreshCalls >= 1)
    }
}
