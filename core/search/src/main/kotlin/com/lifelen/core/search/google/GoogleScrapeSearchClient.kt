package com.lifelen.core.search.google

import com.lifelen.core.search.SearchClient
import com.lifelen.core.search.SearchResult
import java.net.URI
import java.net.URLDecoder
import javax.inject.Inject

/**
 * A keyless [SearchClient] that scrapes the public Google results page. This is the fallback used
 * when no Serper key is configured, so pricing still has something to ground on. It is best-effort:
 * Google's markup shifts and can rate-limit, so any failure degrades to an empty list (no price),
 * exactly as if the search returned nothing.
 */
class GoogleScrapeSearchClient @Inject constructor(
    private val api: GoogleScrapeApi,
) : SearchClient {

    override suspend fun searchShopping(query: String): List<SearchResult> = runCatching {
        parse(api.search(q = "$query price").string())
    }.getOrDefault(emptyList())

    private fun parse(html: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val seenHosts = mutableSetOf<String>()
        for (match in RESULT_RE.findAll(html)) {
            if (results.size >= MAX_RESULTS) break
            val rawUrl = match.groupValues[1]
            val url = runCatching { URLDecoder.decode(rawUrl, "UTF-8") }.getOrDefault(rawUrl)
            val host = url.hostOrNull() ?: continue
            if (host in SKIP_HOSTS || !seenHosts.add(host)) continue
            val title = match.groupValues[2].stripHtml()
            if (title.isBlank()) continue
            // A price near the result, if the snippet carries one.
            val windowEnd = (match.range.last + PRICE_WINDOW).coerceAtMost(html.length)
            val price = PRICE_RE.find(html.substring(match.range.last, windowEnd))?.value
            results += SearchResult(title = title, source = host, url = url, price = price)
        }
        return results
    }

    private fun String.hostOrNull(): String? =
        runCatching { URI(this).host }.getOrNull()?.removePrefix("www.")

    private fun String.stripHtml(): String =
        replace(TAG_RE, "")
            .replace("&amp;", "&")
            .replace("&#39;", "'")
            .replace("&quot;", "\"")
            .replace("&nbsp;", " ")
            .trim()

    private companion object {
        const val MAX_RESULTS = 10
        const val PRICE_WINDOW = 600
        // Result anchors on the Google results page wrap a real URL in /url?q=… followed by an <h3> title.
        val RESULT_RE = Regex(
            """<a href="/url\?q=(https?://[^&"]+)[^"]*"[^>]*>.*?<h3[^>]*>(.*?)</h3>""",
            RegexOption.DOT_MATCHES_ALL,
        )
        val TAG_RE = Regex("<[^>]+>")
        val PRICE_RE = Regex("""[$£€]\s?\d[\d,]*(?:\.\d{2})?""")
        val SKIP_HOSTS = setOf(
            "google.com",
            "webcache.googleusercontent.com",
            "support.google.com",
            "accounts.google.com",
            "policies.google.com",
            "maps.google.com",
            "youtube.com",
        )
    }
}
