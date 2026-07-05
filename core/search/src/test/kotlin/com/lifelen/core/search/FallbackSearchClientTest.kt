package com.lifelen.core.search

import com.lifelen.core.common.network.ApiKeyProvider
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class FallbackSearchClientTest {

    private lateinit var server: MockWebServer
    private lateinit var serperApi: SerperApi
    private lateinit var googleApi: GoogleScrapeApi

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        val json = Json { ignoreUnknownKeys = true }
        val base = server.url("/")
        serperApi = Retrofit.Builder()
            .baseUrl(base)
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SerperApi::class.java)
        googleApi = Retrofit.Builder().baseUrl(base).client(OkHttpClient()).build()
            .create(GoogleScrapeApi::class.java)
    }

    @After
    fun tearDown() = server.shutdown()

    private fun fakeKeys(searchKey: String) = object : ApiKeyProvider {
        override suspend fun dashScopeApiKey(): String = ""
        override suspend fun searchApiKey(): String = searchKey
    }

    private fun fallback(searchKey: String) = FallbackSearchClient(
        serper = SerperSearchClient(serperApi, fakeKeys(searchKey)),
        google = GoogleScrapeSearchClient(googleApi),
        apiKeyProvider = fakeKeys(searchKey),
    )

    @Test
    fun `uses Serper when a search key is configured`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"shopping":[{"title":"MacBook","source":"Amazon","link":"http://a","price":"${'$'}1"}]}"""),
        )

        val results = fallback("KEY").searchShopping("x")

        assertEquals(1, results.size)
        assertEquals("MacBook", results.first().title)
        assertEquals("/shopping", server.takeRequest().path)
    }

    @Test
    fun `falls back to Google scrape when no search key is set`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""<a href="/url?q=https://www.ebay.com/itm/1&x=1"><h3>MacBook on eBay</h3></a>"""),
        )

        val results = fallback("").searchShopping("x")

        assertEquals(1, results.size)
        assertEquals("ebay.com", results.first().source)
        assertTrue(server.takeRequest().path!!.startsWith("/search"))
    }
}
