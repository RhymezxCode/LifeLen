package com.lifelen.core.data.qwen

import com.lifelen.core.model.ScanCategory
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AnalysisParserTest {

    private val parser = AnalysisParser(Json { ignoreUnknownKeys = true })

    @Test
    fun `parses identification wrapped in markdown fences`() {
        val raw = """
            Sure! Here is the result:
            ```json
            {"title":"MacBook Air M3","category":"electronics","summary":"A laptop.",
             "confidence":0.9,"attributes":{"RAM":"16GB"},"tags":["laptop"],
             "search_query":"MacBook Air M3 price"}
            ```
        """.trimIndent()

        val parsed = parser.parseAnalysis(raw)

        assertEquals("MacBook Air M3", parsed.identification.title)
        assertEquals(ScanCategory.ELECTRONICS, parsed.identification.category)
        assertEquals("16GB", parsed.identification.attributes["RAM"])
        assertNull(parsed.nutrition)
    }

    @Test
    fun `parses food nutrition inline`() {
        val raw = """
            {"title":"Cheeseburger","category":"food","summary":"A burger.","confidence":0.8,
             "nutrition":{"serving_size":"1 burger","calories":550,"protein":25.0,"carbs":40.0,"fat":30.0}}
        """.trimIndent()

        val parsed = parser.parseAnalysis(raw)

        assertEquals(ScanCategory.FOOD, parsed.identification.category)
        assertNotNull(parsed.nutrition)
        assertEquals(550, parsed.nutrition!!.calories)
    }

    @Test
    fun `returns null price when model has no data`() {
        val raw = """{"currency":"USD","low_price":0,"high_price":0,"options":[]}"""
        assertNull(parser.parsePrice(raw))
    }

    @Test
    fun `parsePrice returns null for non-JSON prose`() {
        assertNull(parser.parsePrice("The model refused and returned only prose."))
    }

    @Test
    fun `parseAnalysis throws when the response has no JSON object`() {
        try {
            parser.parseAnalysis("no json object here")
            org.junit.Assert.fail("expected an error for content without a JSON object")
        } catch (e: Exception) {
            // extractJsonObject errors when there is no '{...}' — the repository maps this to DataResult.Error.
        }
    }

    @Test
    fun `parses full nutrition fields`() {
        val raw = """
            {"title":"Jollof rice","category":"food","confidence":0.8,
             "nutrition":{"serving_size":"1 plate","calories":540,"protein":28,"carbs":62,"fat":19,
                          "fiber":4,"sugars":6,"sodium":890,"ingredients":["rice","chicken","sauce"]}}
        """.trimIndent()
        val n = parser.parseAnalysis(raw).nutrition!!
        assertEquals(4.0, n.fiber, 0.001)
        assertEquals(6.0, n.sugars, 0.001)
        assertEquals(890, n.sodium)
        assertEquals(listOf("rice", "chicken", "sauce"), n.ingredients)
    }

    @Test
    fun `parses price with conditions, meta and average`() {
        val raw = """
            {"currency":"$","low_price":849,"high_price":999,"average":967,"source":"Google Shopping",
             "options":[
               {"retailer":"Amazon","price":849,"currency":"$","url":"a","in_stock":true,"condition":"new","meta":"Free shipping · in stock"},
               {"retailer":"eBay","price":699,"currency":"$","url":"b","condition":"renewed","meta":"1 yr warranty"}
             ]}
        """.trimIndent()
        val price = parser.parsePrice(raw)!!
        assertEquals(967.0, price.average, 0.001)
        assertEquals("Google Shopping", price.source)
        assertEquals(2, price.options.size)
        val amazon = price.options.first { it.retailer == "Amazon" }
        assertEquals(com.lifelen.core.model.PriceCondition.NEW, amazon.condition)
        assertEquals("Free shipping · in stock", amazon.meta)
        assertEquals(com.lifelen.core.model.PriceCondition.RENEWED, price.options.first { it.retailer == "eBay" }.condition)
        assertEquals(1, price.sellerCount) // only Amazon is NEW
    }
}
