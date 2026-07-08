package com.lifelen.core.search.bing

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

class BingSearchClientTest {

    private lateinit var server: MockWebServer
    private lateinit var api: BingScrapeApi

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .build()
            .create(BingScrapeApi::class.java)
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `parses b_algo results, skips bing chrome and captures a nearby price`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                <li class="b_algo"><h2><a href="https://www.amazon.com/dp/x">MacBook Air M2 — Amazon</a></h2><div>${'$'}999.00</div></li>
                <li class="b_algo"><h2><a href="https://www.bing.com/aclick?u=ad">Sponsored</a></h2></li>
                <li class="b_algo"><h2><a href="https://www.bestbuy.com/y">MacBook — Best Buy</a></h2></li>
                """.trimIndent(),
            ),
        )
        val results = BingSearchClient(api).searchShopping("macbook air")

        assertEquals(2, results.size) // the bing.com ad row is filtered out
        assertEquals("amazon.com", results[0].source)
        assertEquals("https://www.amazon.com/dp/x", results[0].url)
        assertEquals("${'$'}999.00", results[0].price)
        assertEquals("bestbuy.com", results[1].source)
        assertTrue(server.takeRequest().path!!.startsWith("/search"))
    }

    @Test
    fun `returns empty when the page has no parseable results`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("<html><body>No results</body></html>"))

        assertEquals(emptyList<Any>(), BingSearchClient(api).searchShopping("x"))
    }
}
