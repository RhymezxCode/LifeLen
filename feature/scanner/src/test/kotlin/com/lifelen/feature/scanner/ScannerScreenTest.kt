package com.lifelen.feature.scanner

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.lifelen.core.designsystem.theme.LifeLensTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Under Robolectric the CAMERA permission is denied by default, so [ScannerScreen] renders the
 * S01 permission prime rather than the CameraX viewfinder (which cannot render off-device).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScannerScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `permission prime is shown when camera access is denied`() {
        compose.setContent {
            LifeLensTheme {
                ScannerScreen(
                    uiState = ScannerUiState(),
                    onCaptured = {},
                    onCaptureError = {},
                    onDismissError = {},
                    onSelectMode = {},
                    onOpenLibrary = {},
                    onOpenSettings = {},
                )
            }
        }

        compose.onNodeWithText("Enable camera").assertIsDisplayed()
        compose.onNodeWithText("Import a photo instead").assertIsDisplayed()
    }
}
