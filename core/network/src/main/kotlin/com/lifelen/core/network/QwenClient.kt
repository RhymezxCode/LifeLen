package com.lifelen.core.network

import com.lifelen.core.network.model.ChatCompletionRequest
import com.lifelen.core.network.model.ChatMessage
import com.lifelen.core.network.model.ContentPart
import com.lifelen.core.network.model.ImageUrl
import javax.inject.Inject

/**
 * Thin, domain-agnostic wrapper over Qwen. It returns the assistant's raw text; callers in
 * `:core:data` own the prompts and parse the JSON the prompts ask the model to produce.
 */
interface QwenClient {

    /** Sends an image (as a base64 data URL) plus instructions; returns the assistant text. */
    suspend fun analyzeImage(
        imageDataUrl: String,
        systemPrompt: String,
        userPrompt: String,
    ): String

    /** Text-only completion, used for search-grounded price synthesis. */
    suspend fun chat(systemPrompt: String, userPrompt: String): String
}

class QwenClientImpl @Inject constructor(
    private val api: QwenApi,
) : QwenClient {

    override suspend fun analyzeImage(
        imageDataUrl: String,
        systemPrompt: String,
        userPrompt: String,
    ): String {
        val response = api.chatCompletions(
            ChatCompletionRequest(
                model = QwenApi.VISION_MODEL,
                messages = listOf(
                    ChatMessage("system", listOf(ContentPart.Text(systemPrompt))),
                    ChatMessage(
                        role = "user",
                        content = listOf(
                            ContentPart.Image(ImageUrl(imageDataUrl)),
                            ContentPart.Text(userPrompt),
                        ),
                    ),
                ),
            ),
        )
        return response.firstContent()
    }

    override suspend fun chat(systemPrompt: String, userPrompt: String): String {
        val response = api.chatCompletions(
            ChatCompletionRequest(
                model = QwenApi.TEXT_MODEL,
                messages = listOf(
                    ChatMessage("system", listOf(ContentPart.Text(systemPrompt))),
                    ChatMessage("user", listOf(ContentPart.Text(userPrompt))),
                ),
            ),
        )
        return response.firstContent()
    }

    private fun com.lifelen.core.network.model.ChatCompletionResponse.firstContent(): String =
        choices.firstOrNull()?.message?.content?.trim()
            ?: throw IllegalStateException("Qwen returned no choices")
}
