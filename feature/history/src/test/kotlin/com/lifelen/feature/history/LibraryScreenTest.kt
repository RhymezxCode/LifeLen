package com.lifelen.feature.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lifelen.core.designsystem.theme.LifeLensTheme
import com.lifelen.core.model.Identification
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.model.Scan
import com.lifelen.core.model.ScanCategory
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/** Robolectric + Compose test rule against the stateless [LibraryScreen] (S08 / empty S10). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LibraryScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private fun sampleScan() = Scan(
        id = "1",
        imagePath = "/e.jpg",
        identification = Identification(
            title = "MacBook Air 13-inch",
            category = ScanCategory.ELECTRONICS,
            summary = "",
            confidence = 0.94f,
        ),
        price = PriceInfo(currency = "$", lowPrice = 849.0, highPrice = 999.0),
        createdAt = 1_700_000_000_000L,
    )

    private fun render(uiState: LibraryUiState, onNewScan: () -> Unit = {}) {
        compose.setContent {
            LifeLensTheme {
                LibraryScreen(
                    uiState = uiState,
                    onQueryChange = {},
                    onFilter = {},
                    onOpenScan = {},
                    onNewScan = onNewScan,
                    onBack = {},
                )
            }
        }
    }

    @Test
    fun `populated library shows grouped scans and the scan button`() {
        render(
            LibraryUiState(
                isLoading = false,
                totalCount = 1,
                groups = listOf(LibraryGroup(header = "Today", scans = listOf(sampleScan()))),
            ),
        )

        compose.onNodeWithText("Today").assertExists()
        compose.onNodeWithText("MacBook Air 13-inch").assertExists()
        compose.onNodeWithText("New scan").assertExists()
    }

    @Test
    fun `empty library shows the empty state`() {
        render(LibraryUiState(isLoading = false, groups = emptyList(), totalCount = 0))

        compose.onNodeWithText("Nothing scanned yet").assertIsDisplayed()
    }

    @Test
    fun `tapping the scan button invokes onNewScan`() {
        var newScan = false
        render(
            LibraryUiState(
                isLoading = false,
                totalCount = 1,
                groups = listOf(LibraryGroup(header = "Today", scans = listOf(sampleScan()))),
            ),
            onNewScan = { newScan = true },
        )

        compose.onNodeWithText("New scan").performClick()

        assertTrue(newScan)
    }
}
