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
) {
    val category: ScanCategory get() = identification.category
    val title: String get() = identification.title
}
