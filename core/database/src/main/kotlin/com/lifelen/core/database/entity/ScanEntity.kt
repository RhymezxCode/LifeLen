package com.lifelen.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted scan row. Rich objects (identification/nutrition/price) are stored as JSON
 * strings so the database module stays free of the domain-model and serialization deps;
 * mapping to/from domain happens in `:core:data`. [title] and [category] are denormalised
 * for fast ordering and search.
 */
@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey val id: String,
    val imagePath: String,
    val title: String,
    val category: String,
    val identificationJson: String,
    val nutritionJson: String?,
    val priceJson: String?,
    val createdAt: Long,
    val isFavorite: Boolean,
    val previousLowPrice: Double? = null,
)
