package com.lifelen.feature.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.lifelen.core.designsystem.theme.LifeLensTheme
import com.lifelen.core.model.Identification
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.model.Scan
import com.lifelen.core.model.ScanCategory
import org.junit.Assert.assertEquals
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

    private fun render(
        uiState: LibraryUiState,
        onNewScan: () -> Unit = {},
        onQueryChange: (String) -> Unit = {},
        onFilter: (ScanCategory?) -> Unit = {},
        onOpenScan: (String) -> Unit = {},
    ) {
        compose.setContent {
            LifeLensTheme {
                LibraryScreen(
                    uiState = uiState,
                    onQueryChange = onQueryChange,
                    onFilter = onFilter,
                    onOpenScan = onOpenScan,
                    onNewScan = onNewScan,
                    onBack = {},
                )
            }
        }
    }

    private fun populated(scan: Scan = sampleScan()) = LibraryUiState(
        isLoading = false,
        totalCount = 1,
        groups = listOf(LibraryGroup(header = "Today", scans = listOf(scan))),
    )

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

    @Test
    fun `tapping a filter chip reports the category`() {
        var filter: ScanCategory? = null
        var called = false
        render(populated(), onFilter = { filter = it; called = true })

        compose.onNodeWithText("Electronics").performClick()

        assertTrue(called)
        assertEquals(ScanCategory.ELECTRONICS, filter)
    }

    @Test
    fun `tapping a scan row opens that scan`() {
        var openedId: String? = null
        render(populated(), onOpenScan = { openedId = it })

        compose.onNodeWithText("MacBook Air 13-inch").performClick()

        assertEquals("1", openedId)
    }

    @Test
    fun `typing in the search field reports the query`() {
        var query = ""
        render(populated(), onQueryChange = { query = it })

        compose.onNode(hasSetTextAction()).performTextInput("mac")

        assertEquals("mac", query)
    }
}
