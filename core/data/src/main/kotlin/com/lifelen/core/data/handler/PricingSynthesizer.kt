package com.lifelen.core.data.handler

import com.lifelen.core.data.model.ScanOptions
import com.lifelen.core.data.qwen.AnalysisParser
import com.lifelen.core.data.qwen.QwenPrompts
import com.lifelen.core.model.Identification
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.network.QwenClient
import com.lifelen.core.search.SearchClient
import javax.inject.Inject

/**
 * The search-grounding pipeline: query a shopping API for live listings, then ask Qwen to
 * synthesise them into a price range with buy links. Shared by all product handlers.
 */
class PricingSynthesizer @Inject constructor(
    private val searchClient: SearchClient,
    private val qwenClient: QwenClient,
    private val parser: AnalysisParser,
) {
    suspend fun priceFor(identification: Identification, options: ScanOptions): PriceInfo? {
        if (!options.pricingEnabled) return null
        val query = identification.searchQuery?.takeIf { it.isNotBlank() } ?: identification.title
        val results = runCatching { searchClient.searchShopping(query) }.getOrDefault(emptyList())
        if (results.isEmpty()) return null

        val resultsBlock = results.take(10).joinToString("\n") { r ->
            "- ${r.title} | ${r.source} | ${r.price ?: "?"} | ${r.url}"
        }
        val raw = runCatching {
            qwenClient.chat(
                QwenPrompts.PRICE_SYSTEM,
                QwenPrompts.priceUserPrompt(identification.title, resultsBlock),
            )
        }.getOrNull() ?: return null
        return parser.parsePrice(raw)
    }
}
