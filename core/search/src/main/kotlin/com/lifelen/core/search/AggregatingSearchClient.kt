package com.lifelen.core.search

import com.lifelen.core.common.network.ApiKeyProvider
import com.lifelen.core.search.bing.BingSearchClient
import com.lifelen.core.search.duckduckgo.DuckDuckGoSearchClient
import com.lifelen.core.search.google.GoogleScrapeSearchClient
import com.lifelen.core.search.scrape.hostKey
import com.lifelen.core.search.serper.SerperSearchClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * The [SearchClient] the app actually uses. To ground pricing thoroughly it queries several
 * providers **concurrently** and merges their listings:
 *
 *  1. **Google** (keyless scrape) — queried first, so it wins de-duplication ties.
 *  2. **DuckDuckGo** (keyless scrape).
 *  3. **Bing** (keyless scrape).
 *  4. **Serper.dev** (Google Shopping) — added only when a search key is configured.
 *
 * Every provider is best-effort: a failing, rate-limited or empty engine simply contributes
 * nothing, so pricing degrades gracefully to whatever the others found (or to "no price" if all are
 * empty — the same path as before). Listings are deduped to one per retailer host and capped so the
 * downstream Qwen price-synthesis prompt stays bounded.
 */
class AggregatingSearchClient @Inject constructor(
    private val google: GoogleScrapeSearchClient,
    private val duckDuckGo: DuckDuckGoSearchClient,
    private val bing: BingSearchClient,
    private val serper: SerperSearchClient,
    private val apiKeyProvider: ApiKeyProvider,
) : SearchClient {

    override suspend fun searchShopping(query: String): List<SearchResult> = coroutineScope {
        // Order defines de-dup priority — Google first, per the "Google over any other API" rule.
        val providers = buildList {
            add(google)
            add(duckDuckGo)
            add(bing)
            if (apiKeyProvider.searchApiKey().isNotBlank()) add(serper)
        }
        val batches = providers.map { provider ->
            async { runCatching { provider.searchShopping(query) }.getOrDefault(emptyList()) }
        }
        val seenHosts = mutableSetOf<String>()
        batches.awaitAll()
            .flatten()
            .filter { result -> seenHosts.add(result.url.hostKey() ?: result.source.lowercase()) }
            .take(MAX_MERGED_RESULTS)
    }

    private companion object {
        const val MAX_MERGED_RESULTS = 12
    }
}
