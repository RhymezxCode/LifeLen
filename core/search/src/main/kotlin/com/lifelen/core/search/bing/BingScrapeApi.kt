package com.lifelen.core.search.bing

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * Keyless fallback: fetches a Bing results page as raw HTML. A desktop User-Agent keeps Bing on the
 * standard `b_algo` results markup rather than a mobile/JS variant.
 */
interface BingScrapeApi {

    @GET("search")
    @Headers(
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Accept-Language: en-US,en;q=0.9",
    )
    suspend fun search(
        @Query("q") q: String,
        @Query("count") count: Int = 12,
    ): ResponseBody

    companion object {
        const val BASE_URL = "https://www.bing.com/"
    }
}
