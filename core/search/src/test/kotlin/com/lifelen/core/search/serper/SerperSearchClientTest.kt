package com.lifelen.core.search.serper

import com.lifelen.core.common.network.ApiKeyProvider
import com.lifelen.core.search.SearchResult
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

class SerperSearchClientTest {

    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
        encodeDefaults = true
        explicitNulls = false
    }

    private lateinit var server: MockWebServer
    private lateinit var api: SerperApi

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SerperApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun fakeApiKeyProvider(searchKey: String) = object : ApiKeyProvider {
        override suspend fun dashScopeApiKey(): String = ""
        override suspend fun searchApiKey(): String = searchKey
    }

    @Test
    fun `returns empty and makes no request when the search key is blank`() = runTest {
        val client = SerperSearchClient(api, fakeApiKeyProvider(""))

        val results = client.searchShopping("x")

        assertEquals(emptyList<SearchResult>(), results)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `maps items and filters out entries with a blank title or link`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"shopping":[""" +
                    """{"title":"MacBook","source":"Amazon","link":"http://a","price":"$849"},""" +
                    """{"title":"","source":"","link":"","price":null}""" +
                    """]}""",
            ),
        )
        val client = SerperSearchClient(api, fakeApiKeyProvider("K"))

        val results = client.searchShopping("x")

        assertEquals(1, results.size)
        val only = results.first()
        assertEquals("MacBook", only.title)
        assertEquals("Amazon", only.source)
        assertEquals("http://a", only.url)
        assertEquals("$849", only.price)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `maps a blank source to Unknown`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"shopping":[{"title":"Widget","source":"","link":"http://w","price":null}]}""",
            ),
        )
        val client = SerperSearchClient(api, fakeApiKeyProvider("K"))

        val results = client.searchShopping("x")

        assertEquals(1, results.size)
        assertEquals("Unknown", results.first().source)
        assertTrue(results.first().price == null)
    }
}
