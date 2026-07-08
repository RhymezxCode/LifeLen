package com.lifelen.core.search.duckduckgo

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class DuckDuckGoSearchClientTest {

    private lateinit var server: MockWebServer
    private lateinit var api: DuckDuckGoScrapeApi

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .build()
            .create(DuckDuckGoScrapeApi::class.java)
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `decodes uddg redirects into retailer listings`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                <a rel="nofollow" class="result__a" href="//duckduckgo.com/l/?uddg=https%3A%2F%2Fwww.amazon.com%2Fdp%2Fx&rut=1">MacBook Air — Amazon</a>
                <a rel="nofollow" class="result__a" href="//duckduckgo.com/l/?uddg=https%3A%2F%2Fwww.bestbuy.com%2Fy&rut=2">MacBook — Best Buy</a>
                """.trimIndent(),
            ),
        )
        val results = DuckDuckGoSearchClient(api).searchShopping("macbook air")

        assertEquals(2, results.size)
        assertEquals("amazon.com", results[0].source)
        assertEquals("https://www.amazon.com/dp/x", results[0].url)
        assertTrue(results[0].title.contains("MacBook"))
        assertEquals("bestbuy.com", results[1].source)
        assertTrue(server.takeRequest().path!!.startsWith("/html/"))
    }

    @Test
    fun `dedupes multiple listings from the same host`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                <a class="result__a" href="//duckduckgo.com/l/?uddg=https%3A%2F%2Fwww.amazon.com%2Fa">First</a>
                <a class="result__a" href="//duckduckgo.com/l/?uddg=https%3A%2F%2Fwww.amazon.com%2Fb">Second</a>
                """.trimIndent(),
            ),
        )
        val results = DuckDuckGoSearchClient(api).searchShopping("x")

        assertEquals(1, results.size)
        assertEquals("First", results.first().title)
    }

    @Test
    fun `returns empty when the page has no parseable results`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("<html><body>Nothing here</body></html>"))

        assertEquals(emptyList<Any>(), DuckDuckGoSearchClient(api).searchShopping("x"))
    }
}
