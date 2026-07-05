package com.lifelen.feature.prices

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lifelen.core.designsystem.theme.LifeLensTheme
import com.lifelen.core.model.BuyOption
import com.lifelen.core.model.PriceCondition
import com.lifelen.core.model.PriceInfo

private fun laptopPrice() = PriceInfo(
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
)

@Preview(showBackground = true, backgroundColor = 0xFF0D0F13, widthDp = 390, heightDp = 844)
@Composable
private fun PricesScreenPreview() {
    LifeLensTheme {
        PricesScreen(
            uiState = PricesUiState(
                title = "MacBook Air 13\" (M2)",
                price = laptopPrice(),
                selectedCondition = PriceCondition.NEW,
            ),
            onBack = {},
            onRefresh = {},
            onSelectCondition = {},
        )
    }
}
