package com.lifelen.core.model

/**
 * A saved scan: the captured image plus everything LifeLens learned about it.
 * This is the single aggregate persisted to history.
 */
data class Scan(
    val id: String,
    val imagePath: String,
    val identification: Identification,
    val nutrition: NutritionInfo? = null,
    val price: PriceInfo? = null,
    val createdAt: Long,
    val isFavorite: Boolean = false,
    /** Lowest price recorded on a previous fetch — powers the library TrendPill (§3.4). */
    val previousLowPrice: Double? = null,
) {
    val category: ScanCategory get() = identification.category
    val title: String get() = identification.title

    /** Signed change in lowest price since the previous fetch, or null if not comparable. */
    val priceDelta: Double?
        get() {
            val current = price?.lowPrice ?: return null
            val previous = previousLowPrice ?: return null
            val delta = current - previous
            return if (delta == 0.0) null else delta
        }
}
