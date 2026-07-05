package com.lifelen.core.data.qwen

import com.lifelen.core.model.BuyOption
import com.lifelen.core.model.Identification
import com.lifelen.core.model.NutritionInfo
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.model.ScanCategory
import kotlinx.serialization.json.Json
import javax.inject.Inject

/** Identification + any nutrition Qwen returned inline. */
data class ParsedAnalysis(
    val identification: Identification,
    val nutrition: NutritionInfo?,
)

/** Converts Qwen's raw text responses into domain models, tolerating markdown fences. */
class AnalysisParser @Inject constructor(
    private val json: Json,
) {
    fun parseAnalysis(raw: String): ParsedAnalysis {
        val dto = json.decodeFromString<IdentificationDto>(raw.extractJsonObject())
        val identification = Identification(
            title = dto.title,
            category = ScanCategory.fromWireName(dto.category),
            summary = dto.summary,
            confidence = dto.confidence,
            attributes = dto.attributes,
            tags = dto.tags,
            searchQuery = dto.searchQuery,
        )
        val nutrition = dto.nutrition?.toDomain()
        return ParsedAnalysis(identification, nutrition)
    }

    fun parsePrice(raw: String): PriceInfo? {
        val jsonText = raw.extractJsonObjectOrNull() ?: return null
        val dto = runCatching { json.decodeFromString<PriceInfoDto>(jsonText) }.getOrNull() ?: return null
        if (dto.options.isEmpty() && dto.highPrice == 0.0) return null
        return PriceInfo(
            currency = dto.currency,
            lowPrice = dto.lowPrice,
            highPrice = dto.highPrice,
            options = dto.options.map {
                BuyOption(it.retailer, it.price, it.currency, it.url, it.inStock)
            },
            disclaimer = dto.disclaimer,
        )
    }

    private fun NutritionDto.toDomain() = NutritionInfo(
        servingSize = servingSize,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        ingredients = ingredients,
        healthNotes = healthNotes,
    )
}

private fun String.extractJsonObject(): String =
    extractJsonObjectOrNull() ?: error("No JSON object found in model response")

private fun String.extractJsonObjectOrNull(): String? {
    val start = indexOf('{')
    val end = lastIndexOf('}')
    return if (start in 0 until end) substring(start, end + 1) else null
}
