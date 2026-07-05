package com.lifelen.feature.prices

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lifelen.core.designsystem.theme.LifeLensTheme
import com.lifelen.core.model.PriceCondition
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/** Robolectric + Compose test rule against the stateless [PricesScreen]. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PricesScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private fun render(
        uiState: PricesUiState,
        onSelectCondition: (PriceCondition) -> Unit = {},
    ) {
        compose.setContent {
            LifeLensTheme {
                PricesScreen(
                    uiState = uiState,
                    onBack = {},
                    onRefresh = {},
                    onSelectCondition = onSelectCondition,
                )
            }
        }
    }

    @Test
    fun `populated pricing shows the summary and listings`() {
        render(PricesUiState(title = "MacBook Air 13-inch", price = samplePrice()))

        compose.onNodeWithText("Lowest").assertIsDisplayed()
        compose.onNodeWithText("$699").assertIsDisplayed() // lowest summary value
        compose.onNodeWithText("Amazon").assertExists() // a New retailer row
    }

    @Test
    fun `tapping the Renewed chip selects that condition`() {
        var selected: PriceCondition? = null
        render(PricesUiState(title = "MacBook Air 13-inch", price = samplePrice())) { selected = it }

        // "Renewed" is both a filter chip and (on the New tab) a group header; the chip is clickable.
        compose.onAllNodesWithText("Renewed").filterToOne(hasClickAction()).performClick()

        assertEquals(PriceCondition.RENEWED, selected)
    }

    @Test
    fun `the cheapest new listing carries the Best price badge`() {
        render(PricesUiState(title = "MacBook Air 13-inch", price = samplePrice()))

        compose.onNodeWithText("Best price").assertExists()
    }

    @Test
    fun `tapping a seller row confirms before opening an external site`() {
        render(PricesUiState(title = "MacBook Air 13-inch", price = samplePrice()))

        compose.onNodeWithText("Amazon").performClick()

        compose.onNodeWithText("This opens an external site.").assertIsDisplayed()
    }
}
