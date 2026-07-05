package com.lifelen.core.model

import kotlinx.serialization.Serializable

/** Nutrition estimate for a food/meal scan. All macro values are grams per serving. */
@Serializable
data class NutritionInfo(
    val servingSize: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val ingredients: List<String> = emptyList(),
    val healthNotes: String? = null,
)
