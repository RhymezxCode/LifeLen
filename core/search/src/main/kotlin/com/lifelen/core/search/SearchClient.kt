package com.lifelen.core.search

/** A single listing returned by a shopping/web search. */
data class SearchResult(
    val title: String,
    val source: String,
    val url: String,
    val price: String? = null,
    val snippet: String? = null,
)

/**
 * Grounding abstraction: given a product query, return current listings. Swapping providers
 * (Serper, Tavily, SerpAPI, Bing…) means providing a different binding for this interface.
 */
interface SearchClient {
    suspend fun searchShopping(query: String): List<SearchResult>
}
