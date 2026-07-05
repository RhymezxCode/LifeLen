package com.lifelen.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * LifeLens color tokens — Design Spec §2.1. Dark theme only (v1).
 * Alpha-suffixed tokens encode the spec's percentage fills (e.g. 8% = 0x14).
 */

// Surfaces
val Chamber = Color(0xFF0D0F13) // app bg, camera chrome, nav surfaces
val Body = Color(0xFF171A20) // sheets, screens above camera
val Raised = Color(0xFF1F242C) // cards, tiles, inputs, secondary buttons
val Raised2 = Color(0xFF2A303A) // pressed raised, steppers

// Borders
val Hairline = Color(0x14FFFFFF) // white @ 8%
val SubtleBorder = Color(0x1FFFFFFF) // white @ 12%

// Text
val TextPrimary = Color(0xFFF2F4F8)
val TextSecondary = Color(0xFF9AA3B0)
val TextFaint = Color(0xFF6B7480)

// Accent
val Amber = Color(0xFFF2A33C)
val OnAmber = Color(0xFF2A1C05)
val AmberTint = Color(0x29F2A33C) // amber @ 16%

// State
val Positive = Color(0xFF4CC38A)
val Negative = Color(0xFFE5675F)
val PositiveTint = Color(0x244CC38A) // @ 14%
val NegativeTint = Color(0x24E5675F) // @ 14%

// Category accents (chips/icons/macro bar only — never surfaces)
val CatElectronics = Color(0xFF6FB0E8)
val CatFood = Color(0xFFEF8A66)
val CatPlant = Color(0xFF58BD8A)
val CatBook = Color(0xFFA48FE0)
val CatElectronicsTint = Color(0x246FB0E8)
val CatFoodTint = Color(0x24EF8A66)
val CatPlantTint = Color(0x2458BD8A)
val CatBookTint = Color(0x24A48FE0)

// Macro bar segments
val MacroProtein = Color(0xFF6FB0E8)
val MacroCarbs = Color(0xFFF2A33C)
val MacroFat = Color(0xFFA48FE0)

// Media-overlay fills (controls over the viewfinder)
val MediaControlFill = Color(0x17FFFFFF) // white @ ~9%
val SheetGrabber = Color(0xFF3A4048)
