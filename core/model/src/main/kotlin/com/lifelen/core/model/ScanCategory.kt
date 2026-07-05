package com.lifelen.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * High-level classification returned by the vision model. The category drives which
 * [com.lifelen.core.model] fields are populated and which enrichment handler runs
 * (see the CategoryHandler registry in `:core:data`).
 *
 * To support a new object type, add a value here and register a matching handler.
 */
@Serializable
enum class ScanCategory {
    @SerialName("food")
    FOOD,

    @SerialName("electronics")
    ELECTRONICS,

    @SerialName("book")
    BOOK,

    @SerialName("clothing")
    CLOTHING,

    @SerialName("plant")
    PLANT,

    @SerialName("animal")
    ANIMAL,

    @SerialName("landmark")
    LANDMARK,

    @SerialName("document")
    DOCUMENT,

    @SerialName("generic")
    GENERIC;

    companion object {
        fun fromWireName(value: String?): ScanCategory =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: value?.let { raw ->
                    entries.firstOrNull { it.serialName().equals(raw, ignoreCase = true) }
                }
                ?: GENERIC
    }
}

private fun ScanCategory.serialName(): String = name.lowercase()
