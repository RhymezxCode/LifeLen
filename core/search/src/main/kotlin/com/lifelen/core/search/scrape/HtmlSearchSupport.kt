package com.lifelen.core.search.scrape

import java.net.URI

/**
 * Shared helpers for the keyless HTML-scraping search providers (Google, DuckDuckGo, Bing). Each
 * engine's markup differs, so parsing lives in its own client — these are just the common bits:
 * host extraction, HTML unescaping and a nearby-price probe.
 */

/** Cap on results parsed from a single engine before de-duplication. */
internal const val MAX_SCRAPE_RESULTS = 8

/** How far past a result anchor to look for an inline price. */
private const val PRICE_WINDOW = 600

/** Currencies worth recognising in a snippet: $, £, €, ₦ (naira), ₹ (rupee). */
internal val PRICE_RE = Regex("""[$£€₦₹]\s?\d[\d,]*(?:\.\d{2})?""")

private val TAG_RE = Regex("<[^>]+>")

/** Engine chrome / non-retailer hosts to drop from shopping results. */
internal val SKIP_HOSTS = setOf(
    "google.com",
    "webcache.googleusercontent.com",
    "support.google.com",
    "accounts.google.com",
    "policies.google.com",
    "maps.google.com",
    "duckduckgo.com",
    "bing.com",
    "microsofttranslator.com",
    "youtube.com",
    "wikipedia.org",
)

/** Normalised host (no `www.`, lower-cased) used as the cross-engine de-dup key. */
internal fun String.hostKey(): String? =
    runCatching { URI(this).host }.getOrNull()?.removePrefix("www.")?.lowercase()

internal fun String.stripHtml(): String =
    replace(TAG_RE, "")
        .replace("&amp;", "&")
        .replace("&#39;", "'")
        .replace("&#x27;", "'")
        .replace("&quot;", "\"")
        .replace("&nbsp;", " ")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .trim()

/** Best-effort price sitting just after [matchEnd] within [html]. */
internal fun priceNear(html: String, matchEnd: Int): String? {
    val end = (matchEnd + PRICE_WINDOW).coerceAtMost(html.length)
    if (matchEnd >= end) return null
    return PRICE_RE.find(html.substring(matchEnd, end))?.value
}
