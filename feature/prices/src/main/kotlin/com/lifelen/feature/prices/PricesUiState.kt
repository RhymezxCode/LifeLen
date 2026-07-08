package com.lifelen.feature.prices

import com.lifelen.core.model.PriceCondition
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.model.ShoppingLink

/** UI state for the Prices screen (S05). */
data class PricesUiState(
    val title: String = "",
    val price: PriceInfo? = null,
    val selectedCondition: PriceCondition = PriceCondition.NEW,
    val isRefreshing: Boolean = false,
    val notFound: Boolean = false,
    /** Always-available retailer/search-engine links to open and buy the item. */
    val whereToBuy: List<ShoppingLink> = emptyList(),
)
