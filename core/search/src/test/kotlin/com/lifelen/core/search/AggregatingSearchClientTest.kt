package com.lifelen.core.search

import com.lifelen.core.common.network.ApiKeyProvider
import com.lifelen.core.search.bing.BingScrapeApi
import com.lifelen.core.search.bing.BingSearchClient
import com.lifelen.core.search.duckduckgo.DuckDuckGoScrapeApi
import com.lifelen.core.search.duckduckgo.DuckDuckGoSearchClient
import com.lifelen.core.search.google.GoogleScrapeApi
import com.lifelen.core.search.google.GoogleScrapeSearchClient
import com.lifelen.core.search.serper.SerperApi
import com.lifelen.core.search.serper.SerperSearchClient
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Each engine gets its own [MockWebServer], so the concurrent fan-out routes cleanly (Google and
 * Bing would otherwise collide on the `/search` path).
 */
class AggregatingSearchClientTest {

    private val servers = mutableListOf<MockWebServer>()

    private fun serve(body: String): Retrofit {
        val server = MockWebServer().apply {
            start()
            enqueue(MockResponse().setResponseCode(200).setBody(body))
        }
        servers += server
        return Retrofit.Builder().baseUrl(server.url("/")).client(OkHttpClient()).build()
    }

    private fun serveJson(body: String): Retrofit {
        val json = Json { ignoreUnknownKeys = true }
        val server = MockWebServer().apply {
            start()
            enqueue(MockResponse().setResponseCode(200).setBody(body))
        }
        servers += server
        return Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @After
    fun tearDown() = servers.forEach { it.shutdown() }

    private fun keys(searchKey: String) = object : ApiKeyProvider {
        override suspend fun dashScopeApiKey(): String = ""
        override suspend fun searchApiKey(): String = searchKey
    }

    private fun google(body: String) =
        GoogleScrapeSearchClient(serve(body).create(GoogleScrapeApi::class.java))

    private fun duck(body: String) =
        DuckDuckGoSearchClient(serve(body).create(DuckDuckGoScrapeApi::class.java))

    private fun bing(body: String) =
        BingSearchClient(serve(body).create(BingScrapeApi::class.java))

    private fun serper(body: String, key: String) =
        SerperSearchClient(serveJson(body).create(SerperApi::class.java), keys(key))

    @Test
    fun `merges keyless engines, google-first, and dedupes by host`() = runTest {
        val client = AggregatingSearchClient(
            google = google("""<a href="/url?q=https://www.amazon.com/x&sa=U"><h3>Amazon — Google</h3></a>"""),
            duckDuckGo = duck("""<a class="result__a" href="//duckduckgo.com/l/?uddg=https%3A%2F%2Fwww.bestbuy.com%2Fy">Best Buy — DDG</a>"""),
            bing = bing("""<li class="b_algo"><h2><a href="https://www.amazon.com/z">Amazon dup — Bing</a></h2></li>"""),
            serper = serper("{}", ""),
            apiKeyProvider = keys(""),
        )

        val results = client.searchShopping("macbook")

        // amazon (Google) + bestbuy (DDG); Bing's amazon duplicate is dropped.
        assertEquals(2, results.size)
        assertEquals("amazon.com", results[0].source) // Google wins the de-dup tie
        assertEquals("Amazon — Google", results[0].title)
        assertEquals("bestbuy.com", results[1].source)
    }

    @Test
    fun `includes serper only when a search key is configured`() = runTest {
        val client = AggregatingSearchClient(
            google = google("<html>no results</html>"),
            duckDuckGo = duck("<html>no results</html>"),
            bing = bing("<html>no results</html>"),
            serper = serper(
                """{"shopping":[{"title":"MacBook","source":"Amazon","link":"https://a.com","price":"${'$'}1"}]}""",
                "KEY",
            ),
            apiKeyProvider = keys("KEY"),
        )

        val results = client.searchShopping("x")

        assertEquals(1, results.size)
        assertEquals("MacBook", results[0].title)
    }
}
