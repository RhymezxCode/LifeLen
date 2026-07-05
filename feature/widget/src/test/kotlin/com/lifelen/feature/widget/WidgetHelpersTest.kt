package com.lifelen.feature.widget

import com.lifelen.core.model.Identification
import com.lifelen.core.model.NutritionInfo
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.model.Scan
import com.lifelen.core.model.ScanCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM tests for the widget formatting helpers in WidgetCommon.kt. They are `internal`, so
 * this test lives in the same module + package to reach them. The Glance/Context-bound helpers
 * ([loadScans], [rootModifier]) are not covered here because they require the Android/Hilt runtime.
 */
class WidgetHelpersTest {

    private val dayMs = 24 * 60 * 60 * 1000L

    private fun scan(
        category: ScanCategory,
        nutrition: NutritionInfo? = null,
        price: PriceInfo? = null,
    ) = Scan(
        id = "id",
        imagePath = "/id.jpg",
        identification = Identification("Item", category, "summary", 0.9f),
        nutrition = nutrition,
        price = price,
        createdAt = 0L,
    )

    @Test
    fun `isToday is true for now and false for two days ago`() {
        val now = System.currentTimeMillis()
        assertTrue(isToday(now))
        assertFalse(isToday(now - 2 * dayMs))
    }

    @Test
    fun `money formats whole values without decimals`() {
        assertEquals("12", money(12.0))
        assertEquals("849", money(849.0))
    }

    @Test
    fun `money formats fractional values with two decimals`() {
        assertEquals("12.99", money(12.99))
        assertEquals("12.50", money(12.5))
    }

    @Test
    fun `valueLine shows calories for food with nutrition`() {
        val line = scan(
            ScanCategory.FOOD,
            nutrition = NutritionInfo("1 plate", calories = 540, protein = 0.0, carbs = 0.0, fat = 0.0),
        ).valueLine()
        assertEquals("540 kcal", line)
    }

    @Test
    fun `valueLine shows currency and amount for a priced item`() {
        val line = scan(
            ScanCategory.ELECTRONICS,
            price = PriceInfo(currency = "$", lowPrice = 849.0, highPrice = 999.0),
        ).valueLine()
        assertEquals("$849", line)
    }

    @Test
    fun `valueLine falls back to a capitalised category label`() {
        assertEquals("Generic", scan(ScanCategory.GENERIC).valueLine())
        assertEquals("Plant", scan(ScanCategory.PLANT).valueLine())
    }
}
