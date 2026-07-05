@file:OptIn(ExperimentalTextApi::class)

package com.lifelen.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lifelen.core.designsystem.R

/**
 * LifeLens type scale — Design Spec §2.2. Nine styles.
 *
 * The three bundled families match the mockups exactly: **Inter** (body/sans, via one variable TTF
 * exposed at 400/500/600), **Space Grotesk** (display — screen/nav titles, wordmark, empty states),
 * and **JetBrains Mono** (every `data-*` readout — the "instrument" aesthetic). All offline, no
 * downloadable-font runtime dependency.
 */
private fun inter(weight: FontWeight, axis: Int) = Font(
    R.font.inter_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(axis)),
)

private val displayFamily = FontFamily(Font(R.font.space_grotesk_medium, FontWeight.Medium))
private val sansFamily = FontFamily(
    inter(FontWeight.Normal, 400),
    inter(FontWeight.Medium, 500),
    inter(FontWeight.SemiBold, 600),
)
private val monoFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
)

/** Screen titles, wordmark, empty-state headlines. */
val Display = TextStyle(fontFamily = displayFamily, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp)

/** Top-bar / nav titles — the display face at reading size (HTML `--display` 17px/500). */
val NavTitle = TextStyle(fontFamily = displayFamily, fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 22.sp)

/** Sheet titles, item names. */
val TitleStyle = TextStyle(fontFamily = sansFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp)

/** Descriptions, buttons. */
val BodyStyle = TextStyle(fontFamily = sansFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp)

/** Field labels, chips. */
val LabelStyle = TextStyle(fontFamily = sansFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)

/** Timestamps, footnotes, group headers. */
val CaptionStyle = TextStyle(fontFamily = sansFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 14.sp)

/** Hero values (calories, hero price). */
val DataXl = TextStyle(fontFamily = monoFamily, fontWeight = FontWeight.Medium, fontSize = 32.sp, lineHeight = 36.sp)

/** Lowest-price value. */
val DataLg = TextStyle(fontFamily = monoFamily, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 28.sp)

/** Price rows, stat values. */
val DataMd = TextStyle(fontFamily = monoFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp)

/** Confidence %, inline values, portions. */
val DataSm = TextStyle(fontFamily = monoFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)

/** Material3 Typography mapping so stock Material components pick up sensible defaults. */
val LifeLensTypography = Typography(
    titleLarge = Display,
    titleMedium = TitleStyle,
    bodyLarge = BodyStyle,
    bodyMedium = BodyStyle,
    labelLarge = LabelStyle,
    labelMedium = LabelStyle,
    bodySmall = CaptionStyle,
)
