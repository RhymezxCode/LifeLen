package com.lifelen.feature.results

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.lifelen.core.designsystem.theme.LifeLensTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Robolectric + Compose test rule. The stateless [ResultsScreen] is driven directly with each
 * [ResultsUiState]. `capturedImagePath = null` skips the frozen-capture image; the IdentityHeader
 * still hosts a Coil AsyncImage, which composes cleanly under Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ResultsScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private fun render(uiState: ResultsUiState, onSave: () -> Unit = {}) {
        compose.setContent {
            LifeLensTheme {
                ResultsScreen(
                    uiState = uiState,
                    capturedImagePath = null,
                    savedPillVisible = false,
                    onBack = {},
                    onRetake = {},
                    onRefresh = {},
                    onSave = onSave,
                    onSetPortion = {},
                    onOpenPrices = {},
                )
            }
        }
    }

    @Test
    fun `electronics ready shows title, a spec value and the save action`() {
        render(ResultsUiState.Ready(sampleElectronics(), saved = false))

        compose.onNodeWithText("MacBook Air 13-inch").assertExists()
        compose.onNodeWithText("M2").assertExists() // StatTile value from the "Chip" attribute
        compose.onNodeWithText("Save to library").assertExists()
    }

    @Test
    fun `food ready shows the calorie unit`() {
        render(ResultsUiState.Ready(sampleFood(), saved = false))

        // "kcal" appears both as the hero unit and in the "% of a 2,000 kcal day" caption.
        compose.onAllNodesWithText("kcal", substring = true).onFirst().assertExists()
    }

    @Test
    fun `processing state renders without crashing`() {
        render(ResultsUiState.Processing)

        // The capture chrome (close control) is always present; the sheet shows a skeleton.
        compose.onNodeWithContentDescription("Close").assertIsDisplayed()
    }

    @Test
    fun `failed state surfaces the error message`() {
        render(ResultsUiState.Failed("boom"))

        compose.onNodeWithText("boom", substring = true).assertExists()
    }

    @Test
    fun `tapping save invokes onSave`() {
        var saved = false
        render(ResultsUiState.Ready(sampleElectronics(), saved = false), onSave = { saved = true })

        compose.onNodeWithText("Save to library").performScrollTo().performClick()

        assertTrue(saved)
    }
}
