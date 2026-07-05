package com.lifelen.core.model

import kotlinx.serialization.Serializable

/** Nutrition estimate for a food/meal scan. Macro values are grams per serving; sodium is mg. */
@Serializable
data class NutritionInfo(
    val servingSize: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double = 0.0,
    val sugars: Double = 0.0,
    val sodium: Int = 0,
    val ingredients: List<String> = emptyList(),
    val healthNotes: String? = null,
) {
    /** Share of a 2,000 kcal reference day, rounded to a whole percent. */
    val percentOfDailyCalories: Int get() = (calories * 100 / 2000)
}
