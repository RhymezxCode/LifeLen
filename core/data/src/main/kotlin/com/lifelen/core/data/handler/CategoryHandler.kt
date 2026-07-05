package com.lifelen.core.data.handler

import com.lifelen.core.data.model.ScanOptions
import com.lifelen.core.model.Identification
import com.lifelen.core.model.NutritionInfo
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.model.ScanCategory

/** Extra data a handler produced for a scan (nutrition, pricing, …). */
data class Enrichment(
    val nutrition: NutritionInfo? = null,
    val price: PriceInfo? = null,
)

/**
 * Strategy for enriching an identified object of one [category].
 *
 * To support a new object type: add a [ScanCategory], implement a handler, and bind it
 * `@IntoSet` in `DataModule`. Nothing else needs to change — the registry routes to it.
 */
interface CategoryHandler {
    val category: ScanCategory

    suspend fun enrich(
        identification: Identification,
        parsedNutrition: NutritionInfo?,
        options: ScanOptions,
    ): Enrichment
}
