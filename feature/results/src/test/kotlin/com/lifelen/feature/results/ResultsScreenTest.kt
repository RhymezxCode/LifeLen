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
import org.junit.Assert.assertEquals
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

    private fun render(
        uiState: ResultsUiState,
        onSave: () -> Unit = {},
        onSetPortion: (Float) -> Unit = {},
        onOpenPrices: (String) -> Unit = {},
        onToggleFavorite: () -> Unit = {},
        onDelete: () -> Unit = {},
        onRetry: () -> Unit = {},
    ) {
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
                    onSetPortion = onSetPortion,
                    onOpenPrices = onOpenPrices,
                    onToggleFavorite = onToggleFavorite,
                    onDelete = onDelete,
                    onRetry = onRetry,
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

    @Test
    fun `not found state shows the not-found message`() {
        render(ResultsUiState.NotFound)

        compose.onNodeWithText("Scan not found").assertExists()
    }

    @Test
    fun `low confidence prefixes the title with Looks like`() {
        val lowConf = sampleElectronics().let {
            it.copy(identification = it.identification.copy(confidence = 0.5f))
        }
        render(ResultsUiState.Ready(lowConf, saved = false))

        compose.onNodeWithText("Looks like", substring = true).assertExists()
    }

    @Test
    fun `tapping the sellers pill opens prices for the scan`() {
        var openedId: String? = null
        render(ResultsUiState.Ready(sampleElectronics("e1"), saved = false), onOpenPrices = { openedId = it })

        compose.onNodeWithText("sellers", substring = true).performScrollTo().performClick()

        assertEquals("e1", openedId)
    }

    @Test
    fun `stepping the portion invokes onSetPortion`() {
        var stepped = false
        render(ResultsUiState.Ready(sampleFood(), saved = false), onSetPortion = { stepped = true })

        compose.onNodeWithContentDescription("Increase").performScrollTo().performClick()

        assertTrue(stepped)
    }

    @Test
    fun `saved detail exposes favorite and delete controls that fire callbacks`() {
        var favorited = false
        var deleted = false
        render(
            ResultsUiState.Ready(sampleElectronics(), saved = true),
            onToggleFavorite = { favorited = true },
            onDelete = { deleted = true },
        )

        compose.onNodeWithContentDescription("Favorite").performClick()
        assertTrue(favorited)

        compose.onNodeWithContentDescription("Delete").performClick()
        assertTrue(deleted)
    }

    @Test
    fun `fresh scan hides favorite and delete controls`() {
        render(ResultsUiState.Ready(sampleElectronics(), saved = false))

        compose.onNodeWithContentDescription("Favorite").assertDoesNotExist()
        compose.onNodeWithContentDescription("Delete").assertDoesNotExist()
    }

    @Test
    fun `document scan shows the transcribed text`() {
        render(ResultsUiState.Ready(sampleDocument(), saved = false))

        compose.onNodeWithText("Transcribed text").assertExists()
        compose.onNodeWithText("bring the quarterly report", substring = true).assertExists()
    }

    @Test
    fun `offline state shows the message and the last scan`() {
        render(ResultsUiState.Offline(lastScan = sampleElectronics()))

        compose.onNodeWithText("You're offline").assertExists()
        compose.onNodeWithText("MacBook Air 13-inch").assertExists()
        compose.onNodeWithText("Try again").assertExists()
    }

    @Test
    fun `tapping try again on the offline state invokes onRetry`() {
        var retried = false
        render(ResultsUiState.Offline(lastScan = null), onRetry = { retried = true })

        compose.onNodeWithText("Try again").performClick()

        assertTrue(retried)
    }
}
