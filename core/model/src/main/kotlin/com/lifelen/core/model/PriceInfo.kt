package com.lifelen.core.model

import kotlinx.serialization.Serializable

/** A single place to buy the identified product. */
@Serializable
data class BuyOption(
    val retailer: String,
    val price: Double,
    val currency: String,
    val url: String,
    val inStock: Boolean = true,
)

/**
 * Live market pricing synthesised by Qwen from search-grounding results.
 * [disclaimer] is always shown in the UI because prices are estimates, not quotes.
 */
@Serializable
data class PriceInfo(
    val currency: String,
    val lowPrice: Double,
    val highPrice: Double,
    val options: List<BuyOption> = emptyList(),
    val disclaimer: String = "Prices are estimates from public listings and may be out of date.",
) {
    val cheapest: BuyOption? get() = options.minByOrNull { it.price }
}
