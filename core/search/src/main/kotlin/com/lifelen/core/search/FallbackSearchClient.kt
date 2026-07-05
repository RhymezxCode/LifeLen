package com.lifelen.core.search

import com.lifelen.core.common.network.ApiKeyProvider
import com.lifelen.core.search.google.GoogleScrapeSearchClient
import com.lifelen.core.search.serper.SerperSearchClient
import javax.inject.Inject

/**
 * The [SearchClient] the app actually uses: Serper when a search key is configured, otherwise a
 * keyless Google scrape so pricing still works out of the box. If the scrape yields nothing the
 * result is simply an empty list — the same graceful "no price" path as before.
 */
class FallbackSearchClient @Inject constructor(
    private val serper: SerperSearchClient,
    private val google: GoogleScrapeSearchClient,
    private val apiKeyProvider: ApiKeyProvider,
) : SearchClient {

    override suspend fun searchShopping(query: String): List<SearchResult> =
        if (apiKeyProvider.searchApiKey().isNotBlank()) {
            serper.searchShopping(query)
        } else {
            google.searchShopping(query)
        }
}
