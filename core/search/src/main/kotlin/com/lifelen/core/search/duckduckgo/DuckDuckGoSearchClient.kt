package com.lifelen.core.search.duckduckgo

import com.lifelen.core.search.SearchClient
import com.lifelen.core.search.SearchResult
import com.lifelen.core.search.scrape.MAX_SCRAPE_RESULTS
import com.lifelen.core.search.scrape.SKIP_HOSTS
import com.lifelen.core.search.scrape.hostKey
import com.lifelen.core.search.scrape.priceNear
import com.lifelen.core.search.scrape.stripHtml
import java.net.URLDecoder
import javax.inject.Inject

/**
 * A keyless [SearchClient] that scrapes DuckDuckGo's HTML results page. Best-effort like the other
 * scrapers: any failure (markup shift, rate-limit) degrades to an empty list.
 */
class DuckDuckGoSearchClient @Inject constructor(
    private val api: DuckDuckGoScrapeApi,
) : SearchClient {

    override suspend fun searchShopping(query: String): List<SearchResult> = runCatching {
        parse(api.search("$query price").string())
    }.getOrDefault(emptyList())

    private fun parse(html: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val seenHosts = mutableSetOf<String>()
        for (match in RESULT_RE.findAll(html)) {
            if (results.size >= MAX_SCRAPE_RESULTS) break
            val url = extractUrl(match.groupValues[1]) ?: continue
            val host = url.hostKey() ?: continue
            if (host in SKIP_HOSTS || !seenHosts.add(host)) continue
            val title = match.groupValues[2].stripHtml()
            if (title.isBlank()) continue
            results += SearchResult(title = title, source = host, url = url, price = priceNear(html, match.range.last))
        }
        return results
    }

    /** DDG wraps the target in a `/l/?uddg=<encoded>` redirect; occasionally it links directly. */
    private fun extractUrl(href: String): String? {
        val normalized = if (href.startsWith("//")) "https:$href" else href
        val uddg = UDDG_RE.find(normalized)?.groupValues?.get(1)
        return if (uddg != null) {
            runCatching { URLDecoder.decode(uddg, "UTF-8") }.getOrNull()
        } else {
            normalized.takeIf { it.startsWith("http") }
        }
    }

    private companion object {
        // Organic result anchors on the HTML endpoint: <a class="result__a" href="…">Title</a>
        val RESULT_RE = Regex(
            """<a[^>]*class="result__a"[^>]*href="([^"]+)"[^>]*>(.*?)</a>""",
            RegexOption.DOT_MATCHES_ALL,
        )
        val UDDG_RE = Regex("""[?&]uddg=([^&]+)""")
    }
}
