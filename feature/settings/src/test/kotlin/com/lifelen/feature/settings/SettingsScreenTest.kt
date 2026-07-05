package com.lifelen.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.lifelen.core.data.repository.AppSettings
import com.lifelen.core.designsystem.theme.LifeLensTheme
import com.lifelen.core.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/** Robolectric + Compose test rule against the stateless [SettingsScreen]. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private fun render(
        settings: AppSettings = AppSettings(),
        onThemeMode: (ThemeMode) -> Unit = {},
        onClearLibrary: () -> Unit = {},
        onSaveKeys: (String, String) -> Unit = { _, _ -> },
    ) {
        compose.setContent {
            LifeLensTheme {
                SettingsScreen(
                    settings = settings,
                    onSaveKeys = onSaveKeys,
                    onThemeMode = onThemeMode,
                    onPricingChange = {},
                    onHapticsChange = {},
                    onAutoSaveChange = {},
                    onAutoScanChange = {},
                    onRememberKeysChange = {},
                    onClearLibrary = onClearLibrary,
                    onBack = {},
                )
            }
        }
    }

    @Test
    fun `key sections and controls are present`() {
        render()

        compose.onNodeWithText("API keys").assertExists()
        compose.onNodeWithText("System").assertExists()
        compose.onNodeWithText("Light").assertExists()
        compose.onNodeWithText("Dark").assertExists()
        compose.onNodeWithText("Save keys").assertExists()
        compose.onNodeWithText("Live pricing").assertExists()
        compose.onNodeWithText("Clear scan history").assertExists()
    }

    @Test
    fun `tapping a theme chip reports the selection`() {
        var mode: ThemeMode? = null
        render(onThemeMode = { mode = it })

        compose.onNodeWithText("Light").performClick()

        assertEquals(ThemeMode.LIGHT, mode)
    }

    @Test
    fun `clearing history confirms then invokes the callback`() {
        var cleared = false
        render(onClearLibrary = { cleared = true })

        compose.onNodeWithText("Clear scan history").performScrollTo().performClick()

        // The confirmation dialog surfaces the destructive action.
        compose.onNodeWithText("Delete all").assertIsDisplayed()
        compose.onNodeWithText("Delete all").performClick()

        assertTrue(cleared)
    }

    @Test
    fun `tapping Save keys reports the entered keys`() {
        var savedDash: String? = null
        // A non-empty stored key seeds the editable field so Save has something to report.
        render(settings = AppSettings(dashScopeApiKey = "sk-demo"), onSaveKeys = { dash, _ -> savedDash = dash })

        compose.onNodeWithText("Save keys").performScrollTo().performClick()

        assertEquals("sk-demo", savedDash)
    }
}
