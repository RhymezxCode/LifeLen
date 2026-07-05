package com.lifelen.core.search.serper

import com.lifelen.core.common.network.ApiKeyProvider
import com.lifelen.core.search.SearchClient
import com.lifelen.core.search.SearchResult
import javax.inject.Inject

/** [SearchClient] backed by Serper.dev. The API key is read per-call from storage. */
class SerperSearchClient @Inject constructor(
    private val api: SerperApi,
    private val apiKeyProvider: ApiKeyProvider,
) : SearchClient {

    override suspend fun searchShopping(query: String): List<SearchResult> {
        val key = apiKeyProvider.searchApiKey()
        if (key.isBlank()) return emptyList()
        return api.shopping(apiKey = key, body = SerperQuery(q = query))
            .shopping
            .filter { it.title.isNotBlank() && it.link.isNotBlank() }
            .map { item ->
                SearchResult(
                    title = item.title,
                    source = item.source.ifBlank { "Unknown" },
                    url = item.link,
                    price = item.price,
                )
            }
    }
}
