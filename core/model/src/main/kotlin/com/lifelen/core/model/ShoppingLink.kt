package com.lifelen.core.model

import java.net.URLEncoder

/**
 * A tappable link to find/buy a scanned item on a retailer or a shopping search engine. These are
 * generated deterministically from the item's query, so there is **always** somewhere to open and
 * buy — even when live price scraping returns nothing.
 */
data class ShoppingLink(
    val name: String,
    val url: String,
    val kind: Kind,
) {
    enum class Kind { SEARCH_ENGINE, RETAILER }
}

/**
 * Build the standard set of shopping search + retailer links for [query]. Search engines first
 * (broadest), then major retailers to buy from (Amazon, eBay, …). No network or API key needed.
 */
fun shoppingLinksFor(query: String): List<ShoppingLink> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return emptyList()
    val q = URLEncoder.encode(trimmed, "UTF-8")
    return listOf(
        // Shopping search engines — go through these to compare across many sellers.
        ShoppingLink("Google Shopping", "https://www.google.com/search?tbm=shop&q=$q", ShoppingLink.Kind.SEARCH_ENGINE),
        ShoppingLink("Bing Shopping", "https://www.bing.com/shop?q=$q", ShoppingLink.Kind.SEARCH_ENGINE),
        ShoppingLink("DuckDuckGo", "https://duckduckgo.com/?q=$q+buy+price", ShoppingLink.Kind.SEARCH_ENGINE),
        // Retailers — open to buy directly.
        ShoppingLink("Amazon", "https://www.amazon.com/s?k=$q", ShoppingLink.Kind.RETAILER),
        ShoppingLink("eBay", "https://www.ebay.com/sch/i.html?_nkw=$q", ShoppingLink.Kind.RETAILER),
        ShoppingLink("Walmart", "https://www.walmart.com/search?q=$q", ShoppingLink.Kind.RETAILER),
        ShoppingLink("Best Buy", "https://www.bestbuy.com/site/searchpage.jsp?st=$q", ShoppingLink.Kind.RETAILER),
        ShoppingLink("AliExpress", "https://www.aliexpress.com/wholesale?SearchText=$q", ShoppingLink.Kind.RETAILER),
        ShoppingLink("Newegg", "https://www.newegg.com/p/pl?d=$q", ShoppingLink.Kind.RETAILER),
    )
}
