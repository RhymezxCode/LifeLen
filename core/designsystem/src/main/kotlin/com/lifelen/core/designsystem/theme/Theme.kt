package com.lifelen.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.lifelen.core.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Amber,
    onPrimary = OnAmber,
    primaryContainer = AmberTint,
    onPrimaryContainer = Amber,
    secondary = Positive,
    tertiary = CatBook,
    background = DarkPalette.chamber,
    onBackground = DarkPalette.textPrimary,
    surface = DarkPalette.body,
    onSurface = DarkPalette.textPrimary,
    surfaceVariant = DarkPalette.raised,
    onSurfaceVariant = DarkPalette.textSecondary,
    surfaceContainer = DarkPalette.raised,
    surfaceContainerHigh = DarkPalette.raised2,
    outline = DarkPalette.subtleBorder,
    outlineVariant = DarkPalette.hairline,
    error = Negative,
    onError = OnAmber,
    scrim = DarkPalette.chamber,
)

private val LightColorScheme = lightColorScheme(
    primary = Amber,
    onPrimary = OnAmber,
    primaryContainer = AmberTint,
    onPrimaryContainer = Color(0xFF6B4A12),
    secondary = Positive,
    tertiary = CatBook,
    background = LightPalette.chamber,
    onBackground = LightPalette.textPrimary,
    surface = LightPalette.body,
    onSurface = LightPalette.textPrimary,
    surfaceVariant = LightPalette.raised,
    onSurfaceVariant = LightPalette.textSecondary,
    surfaceContainer = LightPalette.raised,
    surfaceContainerHigh = LightPalette.raised2,
    outline = LightPalette.subtleBorder,
    outlineVariant = LightPalette.hairline,
    error = Negative,
    onError = OnAmber,
    scrim = Color(0xFF14181F),
)

/**
 * LifeLens theme. [themeMode] SYSTEM follows the OS setting (Design Spec was dark-only for v1;
 * light mode added on request). Provides both the Material color scheme and the bespoke
 * [LocalLifeLensPalette] that the design-system tokens read from.
 */
@Composable
fun LifeLensTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val palette = if (dark) DarkPalette else LightPalette
    val colorScheme = if (dark) DarkColorScheme else LightColorScheme
    CompositionLocalProvider(LocalLifeLensPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LifeLensTypography,
            content = content,
        )
    }
}
