package com.lifelen.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * OpenAI-compatible chat/completions payloads used against the DashScope endpoint.
 * Message content is always an array of typed parts so text-only and image messages
 * share one shape (the "type" discriminator is emitted by kotlinx.serialization).
 */
@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.2,
    @SerialName("max_tokens") val maxTokens: Int = 1200,
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: List<ContentPart>,
)

@Serializable
sealed interface ContentPart {
    @Serializable
    @SerialName("text")
    data class Text(val text: String) : ContentPart

    @Serializable
    @SerialName("image_url")
    data class Image(@SerialName("image_url") val imageUrl: ImageUrl) : ContentPart
}

@Serializable
data class ImageUrl(val url: String)

@Serializable
data class ChatCompletionResponse(
    val choices: List<Choice> = emptyList(),
)

@Serializable
data class Choice(
    val message: ResponseMessage,
)

@Serializable
data class ResponseMessage(
    val role: String = "assistant",
    val content: String = "",
)
