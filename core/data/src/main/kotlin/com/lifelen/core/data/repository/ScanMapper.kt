package com.lifelen.core.data.repository

import com.lifelen.core.database.entity.ScanEntity
import com.lifelen.core.model.Identification
import com.lifelen.core.model.NutritionInfo
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.model.Scan
import kotlinx.serialization.json.Json
import javax.inject.Inject

/** Serialises the rich domain [Scan] to/from its JSON-backed [ScanEntity]. */
class ScanMapper @Inject constructor(
    private val json: Json,
) {
    fun toEntity(scan: Scan): ScanEntity = ScanEntity(
        id = scan.id,
        imagePath = scan.imagePath,
        title = scan.identification.title,
        category = scan.identification.category.name,
        identificationJson = json.encodeToString<Identification>(scan.identification),
        nutritionJson = scan.nutrition?.let { json.encodeToString<NutritionInfo>(it) },
        priceJson = scan.price?.let { json.encodeToString<PriceInfo>(it) },
        createdAt = scan.createdAt,
        isFavorite = scan.isFavorite,
    )

    fun toDomain(entity: ScanEntity): Scan = Scan(
        id = entity.id,
        imagePath = entity.imagePath,
        identification = json.decodeFromString<Identification>(entity.identificationJson),
        nutrition = entity.nutritionJson?.let { json.decodeFromString<NutritionInfo>(it) },
        price = entity.priceJson?.let { json.decodeFromString<PriceInfo>(it) },
        createdAt = entity.createdAt,
        isFavorite = entity.isFavorite,
    )
}
