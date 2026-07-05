package com.lifelen.core.network

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class QwenClientTest {

    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
        encodeDefaults = true
        explicitNulls = false
    }

    private lateinit var server: MockWebServer
    private lateinit var client: QwenClientImpl

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(QwenApi::class.java)
        client = QwenClientImpl(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `analyzeImage returns the assistant text and sends a vision request`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"choices":[{"message":{"role":"assistant","content":"HELLO"}}]}"""),
        )

        val result = client.analyzeImage(
            imageDataUrl = "data:image/jpeg;base64,XXX",
            systemPrompt = "SYS",
            userPrompt = "USR",
        )

        assertEquals("HELLO", result)

        val body = server.takeRequest().body.readUtf8()
        assertTrue(body, body.contains("\"model\":\"qwen-vl-max\""))
        assertTrue(body, body.contains("\"type\":\"image_url\""))
        assertTrue(body, body.contains("data:image/jpeg;base64,XXX"))
        assertTrue(body, body.contains("\"type\":\"text\""))
        assertTrue(body, body.contains("USR"))
        assertTrue(body, body.contains("SYS"))
    }

    @Test
    fun `chat uses the text model`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"choices":[{"message":{"role":"assistant","content":"HI"}}]}"""),
        )

        val result = client.chat(systemPrompt = "SYS", userPrompt = "USR")

        assertEquals("HI", result)

        val body = server.takeRequest().body.readUtf8()
        assertTrue(body, body.contains("\"model\":\"qwen-plus\""))
    }

    @Test
    fun `analyzeImage throws when the response has no choices`() {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""{"choices":[]}"""),
        )

        assertThrows(IllegalStateException::class.java) {
            runBlocking {
                client.analyzeImage(
                    imageDataUrl = "data:image/jpeg;base64,XXX",
                    systemPrompt = "SYS",
                    userPrompt = "USR",
                )
            }
        }
    }
}
