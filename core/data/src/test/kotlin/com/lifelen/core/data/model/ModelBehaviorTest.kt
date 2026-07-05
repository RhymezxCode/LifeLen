package com.lifelen.core.data.model

import com.lifelen.core.model.BuyOption
import com.lifelen.core.model.Identification
import com.lifelen.core.model.NutritionInfo
import com.lifelen.core.model.PriceCondition
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.model.Scan
import com.lifelen.core.model.ScanCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Behavior of the domain model helpers that drive the UI (design-spec derived values). */
class ModelBehaviorTest {

    @Test
    fun `nutrition percent of daily calories`() {
        assertEquals(27, NutritionInfo("1 plate", 540, 28.0, 62.0, 19.0).percentOfDailyCalories)
        assertEquals(0, NutritionInfo("1", 0, 0.0, 0.0, 0.0).percentOfDailyCalories)
    }

    private fun price(vararg options: BuyOption) =
        PriceInfo(currency = "$", lowPrice = 849.0, highPrice = 999.0, average = 967.0, options = options.toList())

    @Test
    fun `seller count counts only new listings`() {
        val info = price(
            BuyOption("Amazon", 849.0, "$", "u", condition = PriceCondition.NEW),
            BuyOption("Best Buy", 899.0, "$", "u", condition = PriceCondition.NEW),
            BuyOption("eBay", 699.0, "$", "u", condition = PriceCondition.RENEWED),
        )
        assertEquals(2, info.sellerCount)
    }

    @Test
    fun `cheapest vs cheapest new`() {
        val info = price(
            BuyOption("eBay", 699.0, "$", "u", condition = PriceCondition.RENEWED),
            BuyOption("Amazon", 849.0, "$", "u", condition = PriceCondition.NEW),
        )
        assertEquals(699.0, info.cheapest?.price)
        assertEquals(849.0, info.cheapestNew?.price)
    }

    @Test
    fun `options filtered and sorted ascending by condition`() {
        val info = price(
            BuyOption("Target", 999.0, "$", "u", condition = PriceCondition.NEW),
            BuyOption("Amazon", 849.0, "$", "u", condition = PriceCondition.NEW),
            BuyOption("eBay", 699.0, "$", "u", condition = PriceCondition.RENEWED),
        )
        val new = info.options(PriceCondition.NEW)
        assertEquals(listOf(849.0, 999.0), new.map { it.price })
        assertEquals(1, info.options(PriceCondition.RENEWED).size)
        assertEquals(0, info.options(PriceCondition.USED).size)
    }

    private fun scan(low: Double?, previous: Double?): Scan = Scan(
        id = "1",
        imagePath = "/x.jpg",
        identification = Identification("MacBook", ScanCategory.ELECTRONICS, "", 0.9f),
        price = low?.let { PriceInfo("$", it, it, it) },
        createdAt = 0L,
        previousLowPrice = previous,
    )

    @Test
    fun `price delta is null without both prices`() {
        assertNull(scan(low = null, previous = 900.0).priceDelta)
        assertNull(scan(low = 849.0, previous = null).priceDelta)
    }

    @Test
    fun `price delta is null when unchanged, signed otherwise`() {
        assertNull(scan(low = 900.0, previous = 900.0).priceDelta)
        assertEquals(-51.0, scan(low = 849.0, previous = 900.0).priceDelta!!, 0.001)
        assertEquals(10.0, scan(low = 209.0, previous = 199.0).priceDelta!!, 0.001)
    }

    @Test
    fun `category from wire name is case-insensitive with generic fallback`() {
        assertEquals(ScanCategory.FOOD, ScanCategory.fromWireName("food"))
        assertEquals(ScanCategory.ELECTRONICS, ScanCategory.fromWireName("ELECTRONICS"))
        assertEquals(ScanCategory.GENERIC, ScanCategory.fromWireName("spaceship"))
        assertEquals(ScanCategory.GENERIC, ScanCategory.fromWireName(null))
    }
}
