package com.lifelen.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Dark-only Material 3 scheme mapped to LifeLens tokens (§2.1) so stock Material components
 * (TopAppBar, ModalBottomSheet, ripples) inherit the right colors. Bespoke components read the
 * raw tokens directly.
 */
private val LifeLensColorScheme = darkColorScheme(
    primary = Amber,
    onPrimary = OnAmber,
    primaryContainer = AmberTint,
    onPrimaryContainer = Amber,
    secondary = Positive,
    tertiary = CatBook,
    background = Chamber,
    onBackground = TextPrimary,
    surface = Body,
    onSurface = TextPrimary,
    surfaceVariant = Raised,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = Raised,
    surfaceContainerHigh = Raised2,
    outline = SubtleBorder,
    outlineVariant = Hairline,
    error = Negative,
    onError = OnAmber,
    scrim = Chamber,
)

/** Single dark theme for the whole app (Design Spec §1 — dark only for v1). */
@Composable
fun LifeLensTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LifeLensColorScheme,
        typography = LifeLensTypography,
        content = content,
    )
}
