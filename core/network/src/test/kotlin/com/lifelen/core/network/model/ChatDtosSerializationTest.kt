package com.lifelen.core.network.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatDtosSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
        encodeDefaults = true
        explicitNulls = false
    }

    @Test
    fun `encodes a request with typed content parts`() {
        val request = ChatCompletionRequest(
            model = "m",
            messages = listOf(
                ChatMessage(
                    role = "user",
                    content = listOf(
                        ContentPart.Image(ImageUrl("u")),
                        ContentPart.Text("t"),
                    ),
                ),
            ),
        )

        val encoded = json.encodeToString(request)

        assertTrue(encoded, encoded.contains("\"max_tokens\""))
        assertTrue(encoded, encoded.contains("\"type\":\"text\""))
        assertTrue(encoded, encoded.contains("\"type\":\"image_url\""))
        assertTrue(encoded, encoded.contains("\"image_url\":{\"url\":\"u\"}"))
    }

    @Test
    fun `decodes a response and reads the first choice content`() {
        val decoded = json.decodeFromString<ChatCompletionResponse>(
            """{"choices":[{"message":{"content":"x"}}]}""",
        )

        assertEquals("x", decoded.choices.first().message.content)
    }
}
