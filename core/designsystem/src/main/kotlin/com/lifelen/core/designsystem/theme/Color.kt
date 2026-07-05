package com.lifelen.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * LifeLens color tokens — Design Spec §2.1.
 *
 * Accents (amber/state/category/macro) are theme-independent plain vals. The neutral surface/text
 * tokens are theme-aware: they are exposed as `@Composable` getters backed by [LocalLifeLensPalette],
 * so every existing call site (`Chamber`, `TextPrimary`, …) automatically follows light/dark without
 * changing any component code.
 */

// --- Accents (identical in light & dark) ---
val Amber = Color(0xFFF2A33C)
val OnAmber = Color(0xFF2A1C05)
val AmberTint = Color(0x29F2A33C) // amber @ 16%
val Positive = Color(0xFF4CC38A)
val Negative = Color(0xFFE5675F)
val PositiveTint = Color(0x244CC38A) // @ 14%
val NegativeTint = Color(0x24E5675F) // @ 14%

val CatElectronics = Color(0xFF6FB0E8)
val CatFood = Color(0xFFEF8A66)
val CatPlant = Color(0xFF58BD8A)
val CatBook = Color(0xFFA48FE0)
val CatElectronicsTint = Color(0x246FB0E8)
val CatFoodTint = Color(0x24EF8A66)
val CatPlantTint = Color(0x2458BD8A)
val CatBookTint = Color(0x24A48FE0)

val MacroProtein = Color(0xFF6FB0E8)
val MacroCarbs = Color(0xFFF2A33C)
val MacroFat = Color(0xFFA48FE0)

/** Fixed neutral used for chips in non-composable code (e.g. category visuals). */
val NeutralAccent = Color(0xFF8A93A0)

// --- Theme-aware neutral palette ---
data class LifeLensPalette(
    val chamber: Color,
    val body: Color,
    val raised: Color,
    val raised2: Color,
    val hairline: Color,
    val subtleBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textFaint: Color,
    val mediaControlFill: Color,
    val sheetGrabber: Color,
)

val DarkPalette = LifeLensPalette(
    chamber = Color(0xFF0D0F13),
    body = Color(0xFF171A20),
    raised = Color(0xFF1F242C),
    raised2 = Color(0xFF2A303A),
    hairline = Color(0x14FFFFFF),
    subtleBorder = Color(0x1FFFFFFF),
    textPrimary = Color(0xFFF2F4F8),
    textSecondary = Color(0xFF9AA3B0),
    textFaint = Color(0xFF6B7480),
    mediaControlFill = Color(0x17FFFFFF),
    sheetGrabber = Color(0xFF3A4048),
)

/** Light counterpart — same structure; camera chrome stays dark-ish for viewfinder contrast. */
val LightPalette = LifeLensPalette(
    chamber = Color(0xFFF6F8FB),
    body = Color(0xFFFFFFFF),
    raised = Color(0xFFEEF1F6),
    raised2 = Color(0xFFE2E7EE),
    hairline = Color(0x12000000),
    subtleBorder = Color(0x1F000000),
    textPrimary = Color(0xFF14181F),
    textSecondary = Color(0xFF565E6B),
    textFaint = Color(0xFF8A93A0),
    mediaControlFill = Color(0x1F000000),
    sheetGrabber = Color(0xFFC7CDD6),
)

val LocalLifeLensPalette = staticCompositionLocalOf { DarkPalette }

// --- Theme-aware token accessors (unchanged call sites across the app) ---
val Chamber: Color @Composable @ReadOnlyComposable get() = LocalLifeLensPalette.current.chamber
val Body: Color @Composable @ReadOnlyComposable get() = LocalLifeLensPalette.current.body
val Raised: Color @Composable @ReadOnlyComposable get() = LocalLifeLensPalette.current.raised
val Raised2: Color @Composable @ReadOnlyComposable get() = LocalLifeLensPalette.current.raised2
val Hairline: Color @Composable @ReadOnlyComposable get() = LocalLifeLensPalette.current.hairline
val SubtleBorder: Color @Composable @ReadOnlyComposable get() = LocalLifeLensPalette.current.subtleBorder
val TextPrimary: Color @Composable @ReadOnlyComposable get() = LocalLifeLensPalette.current.textPrimary
val TextSecondary: Color @Composable @ReadOnlyComposable get() = LocalLifeLensPalette.current.textSecondary
val TextFaint: Color @Composable @ReadOnlyComposable get() = LocalLifeLensPalette.current.textFaint
val MediaControlFill: Color @Composable @ReadOnlyComposable get() = LocalLifeLensPalette.current.mediaControlFill
val SheetGrabber: Color @Composable @ReadOnlyComposable get() = LocalLifeLensPalette.current.sheetGrabber
