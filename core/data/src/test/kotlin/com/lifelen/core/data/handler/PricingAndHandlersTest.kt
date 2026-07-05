package com.lifelen.core.data.handler

import com.lifelen.core.data.model.ScanOptions
import com.lifelen.core.data.qwen.AnalysisParser
import com.lifelen.core.model.Identification
import com.lifelen.core.model.NutritionInfo
import com.lifelen.core.model.ScanCategory
import com.lifelen.core.network.QwenClient
import com.lifelen.core.search.SearchClient
import com.lifelen.core.search.SearchResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeSearchClient(private val results: List<SearchResult>) : SearchClient {
    var lastQuery: String? = null
    override suspend fun searchShopping(query: String): List<SearchResult> {
        lastQuery = query
        return results
    }
}

private class FakeQwenClient(private val chatResponse: String = "") : QwenClient {
    override suspend fun analyzeImage(imageDataUrl: String, systemPrompt: String, userPrompt: String) = ""
    override suspend fun chat(systemPrompt: String, userPrompt: String) = chatResponse
}

class PricingAndHandlersTest {

    private val parser = AnalysisParser(Json { ignoreUnknownKeys = true })
    private val priceJson = """
        {"currency":"$","low_price":849,"high_price":999,"average":967,"source":"Google Shopping",
         "options":[{"retailer":"Amazon","price":849,"currency":"$","url":"http://a","condition":"new","meta":"Free shipping"}]}
    """.trimIndent()
    private val laptop = Identification("MacBook Air", ScanCategory.ELECTRONICS, "", 0.94f, searchQuery = "macbook air price")
    private val results = listOf(SearchResult("MacBook Air", "Amazon", "http://a", price = "$849"))

    @Test
    fun `pricing returns null when disabled`() = runTest {
        val synth = PricingSynthesizer(FakeSearchClient(results), FakeQwenClient(priceJson), parser)
        assertNull(synth.priceFor(laptop, ScanOptions(pricingEnabled = false)))
    }

    @Test
    fun `pricing returns null when search finds nothing`() = runTest {
        val synth = PricingSynthesizer(FakeSearchClient(emptyList()), FakeQwenClient(priceJson), parser)
        assertNull(synth.priceFor(laptop, ScanOptions(pricingEnabled = true)))
    }

    @Test
    fun `pricing synthesizes from grounded results and uses the search query`() = runTest {
        val search = FakeSearchClient(results)
        val synth = PricingSynthesizer(search, FakeQwenClient(priceJson), parser)
        val price = synth.priceFor(laptop, ScanOptions(pricingEnabled = true))!!
        assertEquals(849.0, price.lowPrice, 0.001)
        assertEquals("Amazon", price.cheapestNew?.retailer)
        assertEquals("macbook air price", search.lastQuery)
    }

    @Test
    fun `food handler surfaces parsed nutrition and no price`() = runTest {
        val nutrition = NutritionInfo("1 plate", 540, 28.0, 62.0, 19.0)
        val enrichment = FoodHandler().enrich(laptop.copy(category = ScanCategory.FOOD), nutrition, ScanOptions(true))
        assertEquals(nutrition, enrichment.nutrition)
        assertNull(enrichment.price)
    }

    @Test
    fun `electronics handler runs the pricing pipeline`() = runTest {
        val synth = PricingSynthesizer(FakeSearchClient(results), FakeQwenClient(priceJson), parser)
        val enrichment = ElectronicsHandler(synth).enrich(laptop, null, ScanOptions(true))
        assertEquals(849.0, enrichment.price?.lowPrice)
    }

    @Test
    fun `plant handler adds no price or nutrition (care lives in attributes)`() = runTest {
        val plant = laptop.copy(category = ScanCategory.PLANT, attributes = mapOf("Light" to "Bright indirect", "Water" to "Weekly"))
        val enrichment = PlantHandler().enrich(plant, null, ScanOptions(true))
        assertNull(enrichment.price)
        assertNull(enrichment.nutrition)
    }

    @Test
    fun `document handler adds no price or nutrition (text lives in attributes)`() = runTest {
        val doc = laptop.copy(category = ScanCategory.DOCUMENT, attributes = mapOf("Text" to "Meeting at 3pm"))
        val enrichment = DocumentHandler().enrich(doc, null, ScanOptions(true))
        assertNull(enrichment.price)
        assertNull(enrichment.nutrition)
    }

    @Test
    fun `registry routes by category and falls back to generic`() {
        val registry = CategoryHandlerRegistry(setOf(GenericHandler(), FoodHandler(), PlantHandler(), DocumentHandler()))
        assertTrue(registry.handlerFor(ScanCategory.FOOD) is FoodHandler)
        assertTrue(registry.handlerFor(ScanCategory.PLANT) is PlantHandler)
        assertTrue(registry.handlerFor(ScanCategory.DOCUMENT) is DocumentHandler)
        assertTrue(registry.handlerFor(ScanCategory.LANDMARK) is GenericHandler)
    }
}
