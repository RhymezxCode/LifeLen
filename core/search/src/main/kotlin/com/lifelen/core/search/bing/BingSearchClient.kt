package com.lifelen.core.search.bing

import com.lifelen.core.search.SearchClient
import com.lifelen.core.search.SearchResult
import com.lifelen.core.search.scrape.MAX_SCRAPE_RESULTS
import com.lifelen.core.search.scrape.SKIP_HOSTS
import com.lifelen.core.search.scrape.hostKey
import com.lifelen.core.search.scrape.priceNear
import com.lifelen.core.search.scrape.stripHtml
import javax.inject.Inject

/**
 * A keyless [SearchClient] that scrapes Bing's organic results. Best-effort like the other
 * scrapers: any failure degrades to an empty list.
 */
class BingSearchClient @Inject constructor(
    private val api: BingScrapeApi,
) : SearchClient {

    override suspend fun searchShopping(query: String): List<SearchResult> = runCatching {
        parse(api.search("$query price").string())
    }.getOrDefault(emptyList())

    private fun parse(html: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val seenHosts = mutableSetOf<String>()
        for (match in RESULT_RE.findAll(html)) {
            if (results.size >= MAX_SCRAPE_RESULTS) break
            val url = match.groupValues[1]
            val host = url.hostKey() ?: continue
            if (host in SKIP_HOSTS || !seenHosts.add(host)) continue
            val title = match.groupValues[2].stripHtml()
            if (title.isBlank()) continue
            results += SearchResult(title = title, source = host, url = url, price = priceNear(html, match.range.last))
        }
        return results
    }

    private companion object {
        // Bing organic results: <li class="b_algo"> … <h2><a href="URL">Title</a></h2>
        val RESULT_RE = Regex(
            """<li class="b_algo">.*?<h2>.*?<a[^>]*href="(https?://[^"]+)"[^>]*>(.*?)</a>""",
            RegexOption.DOT_MATCHES_ALL,
        )
    }
}
