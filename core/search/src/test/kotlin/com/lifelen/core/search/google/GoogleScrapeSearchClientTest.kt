package com.lifelen.core.search.google

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

class GoogleScrapeSearchClientTest {

    private lateinit var server: MockWebServer
    private lateinit var api: GoogleScrapeApi

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .build()
            .create(GoogleScrapeApi::class.java)
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `parses result anchors into listings and skips google hosts`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                <div><a href="/url?q=https://www.amazon.com/dp/x&sa=U"><h3>MacBook Air M2 — Amazon</h3></a><span>${'$'}999.00</span></div>
                <div><a href="/url?q=https://www.bestbuy.com/site/y&sa=U"><h3>MacBook Air (M2) | Best Buy</h3></a><span>${'$'}1,049</span></div>
                <div><a href="/url?q=https://support.google.com/z"><h3>Google support</h3></a></div>
                """.trimIndent(),
            ),
        )
        val results = GoogleScrapeSearchClient(api).searchShopping("macbook air")

        assertEquals(2, results.size) // the support.google.com row is filtered out
        assertEquals("amazon.com", results[0].source)
        assertEquals("https://www.amazon.com/dp/x", results[0].url)
        assertTrue(results[0].title.contains("MacBook"))
        assertEquals("${'$'}999.00", results[0].price)
        assertEquals("bestbuy.com", results[1].source)
    }

    @Test
    fun `dedupes multiple listings from the same host`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                <a href="/url?q=https://www.amazon.com/a&x=1"><h3>First</h3></a>
                <a href="/url?q=https://www.amazon.com/b&x=1"><h3>Second</h3></a>
                """.trimIndent(),
            ),
        )
        val results = GoogleScrapeSearchClient(api).searchShopping("x")

        assertEquals(1, results.size)
        assertEquals("First", results.first().title)
    }

    @Test
    fun `returns empty when the page has no parseable results`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("<html><body>Nothing here</body></html>"))

        assertEquals(emptyList<Any>(), GoogleScrapeSearchClient(api).searchShopping("x"))
    }
}
