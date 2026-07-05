package com.lifelen.core.data.repository

import com.lifelen.core.model.BuyOption
import com.lifelen.core.model.Identification
import com.lifelen.core.model.NutritionInfo
import com.lifelen.core.model.PriceCondition
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.model.Scan
import com.lifelen.core.model.ScanCategory
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Pure round-trip tests for [ScanMapper] — no Android framework needed. */
class ScanMapperTest {

    private val mapper = ScanMapper(Json { ignoreUnknownKeys = true })

    private val electronics = Scan(
        id = "e1",
        imagePath = "/tmp/e1.jpg",
        identification = Identification(
            title = "MacBook Air M3",
            category = ScanCategory.ELECTRONICS,
            summary = "A thin laptop.",
            confidence = 0.92f,
            attributes = mapOf("Chip" to "M3", "RAM" to "16GB"),
            tags = listOf("laptop", "apple"),
            searchQuery = "MacBook Air M3 price",
        ),
        price = PriceInfo(
            currency = "$",
            lowPrice = 849.0,
            highPrice = 999.0,
            average = 924.0,
            options = listOf(
                BuyOption("Amazon", 849.0, "$", "https://a", true, PriceCondition.NEW, "Free shipping"),
            ),
        ),
        createdAt = 3_000L,
        isFavorite = true,
    )

    private val food = Scan(
        id = "f1",
        imagePath = "/tmp/f1.jpg",
        identification = Identification(
            title = "Cheeseburger",
            category = ScanCategory.FOOD,
            summary = "A burger.",
            confidence = 0.8f,
        ),
        nutrition = NutritionInfo(
            servingSize = "1 burger",
            calories = 550,
            protein = 25.0,
            carbs = 40.0,
            fat = 30.0,
            fiber = 3.0,
            sugars = 8.0,
            sodium = 900,
            ingredients = listOf("bun", "beef", "cheese"),
            healthNotes = "High in sodium.",
        ),
        createdAt = 2_000L,
    )

    private val bare = Scan(
        id = "g1",
        imagePath = "/tmp/g1.jpg",
        identification = Identification(
            title = "Mystery object",
            category = ScanCategory.GENERIC,
            summary = "Something.",
        ),
        nutrition = null,
        price = null,
        createdAt = 1_000L,
    )

    private val withPreviousLow = electronics.copy(
        id = "e2",
        previousLowPrice = 799.0,
    )

    @Test
    fun `electronics scan round-trips unchanged`() {
        val restored = mapper.toDomain(mapper.toEntity(electronics))
        assertEquals(electronics, restored)
    }

    @Test
    fun `food scan round-trips unchanged`() {
        val restored = mapper.toDomain(mapper.toEntity(food))
        assertEquals(food, restored)
    }

    @Test
    fun `scan with null price and nutrition round-trips unchanged`() {
        val restored = mapper.toDomain(mapper.toEntity(bare))
        assertEquals(bare, restored)
    }

    @Test
    fun `previousLowPrice survives the round-trip`() {
        val restored = mapper.toDomain(mapper.toEntity(withPreviousLow))
        assertEquals(withPreviousLow, restored)
        assertEquals(799.0, restored.previousLowPrice!!, 0.0001)
    }

    @Test
    fun `title and category are denormalised on the entity`() {
        val entity = mapper.toEntity(electronics)
        assertEquals("MacBook Air M3", entity.title)
        assertEquals("ELECTRONICS", entity.category)
        assertNotNull(entity.priceJson)
        assertNull(entity.nutritionJson)
    }

    @Test
    fun `nutritionJson and priceJson are null when the domain value is null`() {
        val entity = mapper.toEntity(bare)
        assertNull(entity.nutritionJson)
        assertNull(entity.priceJson)
    }

    @Test
    fun `food entity denormalises nutrition and leaves price null`() {
        val entity = mapper.toEntity(food)
        assertEquals("FOOD", entity.category)
        assertNotNull(entity.nutritionJson)
        assertNull(entity.priceJson)
    }
}
