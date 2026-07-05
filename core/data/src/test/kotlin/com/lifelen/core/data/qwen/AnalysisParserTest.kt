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
}
