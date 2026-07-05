package com.lifelen.feature.results

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lifelen.core.designsystem.theme.LifeLensTheme
import com.lifelen.core.model.BuyOption
import com.lifelen.core.model.Identification
import com.lifelen.core.model.NutritionInfo
import com.lifelen.core.model.PriceCondition
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.model.Scan
import com.lifelen.core.model.ScanCategory

// ---------------------------------------------------------------------------
// Preview sample data — realistic scans for the Android Studio preview pane.
// ---------------------------------------------------------------------------

private fun laptopScan() = Scan(
    id = "1",
    imagePath = "",
    createdAt = 0L,
    identification = Identification(
        title = "MacBook Air 13\" (M2, 2022)",
        category = ScanCategory.ELECTRONICS,
        summary = "A thin-and-light laptop.",
        confidence = 0.94f,
        attributes = linkedMapOf(
            "Chip" to "M2",
            "Memory" to "8 GB",
            "Storage" to "256 GB",
            "Display" to "13.6\"",
        ),
    ),
    price = PriceInfo(
        currency = "$",
        lowPrice = 849.0,
        highPrice = 999.0,
        average = 967.0,
        source = "Google Shopping",
        options = listOf(
            BuyOption("Amazon", 849.0, "$", "http://a", true, PriceCondition.NEW, "Free shipping · in stock"),
            BuyOption("Best Buy", 899.0, "$", "http://b", true, PriceCondition.NEW, "Free pickup today"),
            BuyOption("eBay", 699.0, "$", "http://e", true, PriceCondition.RENEWED, "1 yr warranty"),
        ),
    ),
)

private fun foodScan() = Scan(
    id = "2",
    imagePath = "",
    createdAt = 0L,
    identification = Identification(
        title = "Jollof rice with grilled chicken",
        category = ScanCategory.FOOD,
        summary = "",
        confidence = 0.8f,
    ),
    nutrition = NutritionInfo(
        servingSize = "1 plate · ~350 g",
        calories = 540,
        protein = 28.0,
        carbs = 62.0,
        fat = 19.0,
        fiber = 4.0,
        sugars = 6.0,
        sodium = 890,
        ingredients = listOf("rice", "chicken", "sauce"),
    ),
)

// ---------------------------------------------------------------------------
// Previews.
// ---------------------------------------------------------------------------

@Preview(showBackground = true, backgroundColor = 0xFF0D0F13, widthDp = 390, heightDp = 844)
@Composable
private fun ElectronicsResultPreview() {
    LifeLensTheme {
        ResultsScreen(
            uiState = ResultsUiState.Ready(laptopScan(), saved = false),
            capturedImagePath = "",
            savedPillVisible = false,
            onBack = {},
            onRetake = {},
            onRefresh = {},
            onSave = {},
            onSetPortion = {},
            onOpenPrices = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0F13, widthDp = 390, heightDp = 844)
@Composable
private fun FoodResultPreview() {
    LifeLensTheme {
        ResultsScreen(
            uiState = ResultsUiState.Ready(foodScan(), saved = false),
            capturedImagePath = "",
            savedPillVisible = false,
            onBack = {},
            onRetake = {},
            onRefresh = {},
            onSave = {},
            onSetPortion = {},
            onOpenPrices = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0F13, widthDp = 390, heightDp = 844)
@Composable
private fun ProcessingResultPreview() {
    LifeLensTheme {
        ResultsScreen(
            uiState = ResultsUiState.Processing,
            capturedImagePath = "",
            savedPillVisible = false,
            onBack = {},
            onRetake = {},
            onRefresh = {},
            onSave = {},
            onSetPortion = {},
            onOpenPrices = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0F13, widthDp = 390, heightDp = 844)
@Composable
private fun LowConfidenceResultPreview() {
    val scan = laptopScan().let {
        it.copy(identification = it.identification.copy(confidence = 0.52f))
    }
    LifeLensTheme {
        ResultsScreen(
            uiState = ResultsUiState.Ready(scan, saved = false),
            capturedImagePath = "",
            savedPillVisible = false,
            onBack = {},
            onRetake = {},
            onRefresh = {},
            onSave = {},
            onSetPortion = {},
            onOpenPrices = {},
        )
    }
}
