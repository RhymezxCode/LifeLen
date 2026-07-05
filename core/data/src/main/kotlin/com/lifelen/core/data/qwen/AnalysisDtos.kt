package com.lifelen.core.data.qwen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Wire shapes for the JSON Qwen returns; mapped to domain models by [AnalysisParser]. */
@Serializable
data class IdentificationDto(
    val title: String = "Unknown item",
    val category: String = "generic",
    val summary: String = "",
    val confidence: Float = 0f,
    val attributes: Map<String, String> = emptyMap(),
    val tags: List<String> = emptyList(),
    @SerialName("search_query") val searchQuery: String? = null,
    val nutrition: NutritionDto? = null,
)

@Serializable
data class NutritionDto(
    @SerialName("serving_size") val servingSize: String = "1 serving",
    val calories: Int = 0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val ingredients: List<String> = emptyList(),
    @SerialName("health_notes") val healthNotes: String? = null,
)

@Serializable
data class PriceInfoDto(
    val currency: String = "USD",
    @SerialName("low_price") val lowPrice: Double = 0.0,
    @SerialName("high_price") val highPrice: Double = 0.0,
    val options: List<BuyOptionDto> = emptyList(),
    val disclaimer: String = "Prices are estimates from public listings and may be out of date.",
)

@Serializable
data class BuyOptionDto(
    val retailer: String = "",
    val price: Double = 0.0,
    val currency: String = "USD",
    val url: String = "",
    @SerialName("in_stock") val inStock: Boolean = true,
)
