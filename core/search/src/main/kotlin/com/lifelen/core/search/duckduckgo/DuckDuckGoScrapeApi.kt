package com.lifelen.core.search.duckduckgo

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * Keyless fallback: DuckDuckGo's no-JS HTML endpoint returns plain, parseable markup. A desktop
 * User-Agent keeps it on the standard results layout.
 */
interface DuckDuckGoScrapeApi {

    @GET("html/")
    @Headers(
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Accept-Language: en-US,en;q=0.9",
    )
    suspend fun search(@Query("q") q: String): ResponseBody

    companion object {
        const val BASE_URL = "https://html.duckduckgo.com/"
    }
}
