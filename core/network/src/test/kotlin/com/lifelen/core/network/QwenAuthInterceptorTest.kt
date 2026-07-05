package com.lifelen.core.network

import com.lifelen.core.common.network.ApiKeyProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class QwenAuthInterceptorTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun fakeApiKeyProvider(key: String) = object : ApiKeyProvider {
        override suspend fun dashScopeApiKey(): String = key
        override suspend fun searchApiKey(): String = ""
    }

    @Test
    fun `adds bearer authorization and content type headers`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("ok"))

        val client = OkHttpClient.Builder()
            .addInterceptor(QwenAuthInterceptor(fakeApiKeyProvider("KEY")))
            .build()

        client.newCall(Request.Builder().url(server.url("/")).build()).execute().use { }

        val recorded = server.takeRequest()
        assertEquals("Bearer KEY", recorded.getHeader("Authorization"))
        assertNotNull(recorded.getHeader("Content-Type"))
    }
}
