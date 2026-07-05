package com.lifelen.feature.prices

import com.lifelen.core.model.PriceCondition
import com.lifelen.core.model.PriceInfo

/** UI state for the Prices screen (S05). */
data class PricesUiState(
    val title: String = "",
    val price: PriceInfo? = null,
    val selectedCondition: PriceCondition = PriceCondition.NEW,
    val isRefreshing: Boolean = false,
    val notFound: Boolean = false,
)
