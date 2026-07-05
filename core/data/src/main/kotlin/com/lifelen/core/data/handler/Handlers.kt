package com.lifelen.core.data.handler

import com.lifelen.core.data.model.ScanOptions
import com.lifelen.core.model.Identification
import com.lifelen.core.model.NutritionInfo
import com.lifelen.core.model.ScanCategory
import javax.inject.Inject

/** Fallback handler — no extra enrichment beyond the identification itself. */
class GenericHandler @Inject constructor() : CategoryHandler {
    override val category = ScanCategory.GENERIC
    override suspend fun enrich(
        identification: Identification,
        parsedNutrition: NutritionInfo?,
        options: ScanOptions,
    ) = Enrichment()
}

/** Food handler — surfaces the nutrition Qwen estimated for the meal. */
class FoodHandler @Inject constructor() : CategoryHandler {
    override val category = ScanCategory.FOOD
    override suspend fun enrich(
        identification: Identification,
        parsedNutrition: NutritionInfo?,
        options: ScanOptions,
    ) = Enrichment(nutrition = parsedNutrition)
}

/** Base for shoppable object types — runs the search-grounded pricing pipeline. */
abstract class ProductHandler(
    private val pricing: PricingSynthesizer,
) : CategoryHandler {
    override suspend fun enrich(
        identification: Identification,
        parsedNutrition: NutritionInfo?,
        options: ScanOptions,
    ) = Enrichment(price = pricing.priceFor(identification, options))
}

class ElectronicsHandler @Inject constructor(pricing: PricingSynthesizer) : ProductHandler(pricing) {
    override val category = ScanCategory.ELECTRONICS
}

class BookHandler @Inject constructor(pricing: PricingSynthesizer) : ProductHandler(pricing) {
    override val category = ScanCategory.BOOK
}

class ClothingHandler @Inject constructor(pricing: PricingSynthesizer) : ProductHandler(pricing) {
    override val category = ScanCategory.CLOTHING
}
