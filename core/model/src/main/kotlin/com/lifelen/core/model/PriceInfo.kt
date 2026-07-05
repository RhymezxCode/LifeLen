package com.lifelen.core.model

import kotlinx.serialization.Serializable

/** Listing condition — Design Spec §3.3 (New default, Renewed/Used grouped below). */
@Serializable
enum class PriceCondition { NEW, RENEWED, USED }

/** A single place to buy the identified product. */
@Serializable
data class BuyOption(
    val retailer: String,
    val price: Double,
    val currency: String,
    val url: String,
    val inStock: Boolean = true,
    val condition: PriceCondition = PriceCondition.NEW,
    /** Short seller meta, e.g. "Free shipping · in stock". */
    val meta: String? = null,
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
    val average: Double = 0.0,
    val source: String = "Google Shopping",
    val options: List<BuyOption> = emptyList(),
    val disclaimer: String = "Prices are estimates from public listings and may be out of date.",
    /** Epoch millis when this pricing was fetched/synthesised; drives the "updated HH:mm" footnote. */
    val fetchedAt: Long? = null,
) {
    /** Sellers offering the item new (drives the "{n} sellers" pill and summary). */
    val sellerCount: Int get() = options.count { it.condition == PriceCondition.NEW }.takeIf { it > 0 } ?: options.size

    val cheapest: BuyOption? get() = options.minByOrNull { it.price }

    val cheapestNew: BuyOption?
        get() = options.filter { it.condition == PriceCondition.NEW }.minByOrNull { it.price }

    fun options(condition: PriceCondition): List<BuyOption> =
        options.filter { it.condition == condition }.sortedBy { it.price }
}