package com.lifelen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifelen.core.designsystem.component.ConfidenceBadge
import com.lifelen.core.designsystem.component.EmptyState
import com.lifelen.core.designsystem.component.LifeLensButton
import com.lifelen.core.designsystem.theme.LifeLensTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Compose UI tests for the LifeLens design system. Run on a device/emulator:
 * `./gradlew connectedDebugAndroidTest`.
 */
@RunWith(AndroidJUnit4::class)
class LifeLensUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyState_rendersHeadlineAndFiresCta() {
        var clicked = false
        composeRule.setContent {
            LifeLensTheme {
                EmptyState(
                    headline = "Nothing scanned yet",
                    body = "Your identified items will live here.",
                    ctaText = "Start scanning",
                    onCta = { clicked = true },
                )
            }
        }
        composeRule.onNodeWithText("Nothing scanned yet").assertIsDisplayed()
        composeRule.onNodeWithText("Start scanning").performClick()
        assertTrue(clicked)
    }

    @Test
    fun confidenceBadge_showsMatchPercentAboveThreshold() {
        composeRule.setContent { LifeLensTheme { ConfidenceBadge(0.94f) } }
        composeRule.onNodeWithText("94% match").assertIsDisplayed()
    }

    @Test
    fun confidenceBadge_goesNeutralBelowThreshold() {
        composeRule.setContent { LifeLensTheme { ConfidenceBadge(0.52f) } }
        composeRule.onNodeWithText("~52%").assertIsDisplayed()
    }

    @Test
    fun primaryButton_isClickable() {
        var clicked = false
        composeRule.setContent {
            LifeLensTheme { LifeLensButton("Save to library", onClick = { clicked = true }) }
        }
        composeRule.onNodeWithText("Save to library").performClick()
        assertTrue(clicked)
    }
}
