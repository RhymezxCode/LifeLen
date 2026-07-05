package com.lifelen.core.network

import com.lifelen.core.network.model.ChatCompletionRequest
import com.lifelen.core.network.model.ChatCompletionResponse
import retrofit2.http.Body
import retrofit2.http.POST

/** Retrofit binding for the DashScope OpenAI-compatible chat endpoint. */
interface QwenApi {

    @POST("chat/completions")
    suspend fun chatCompletions(@Body request: ChatCompletionRequest): ChatCompletionResponse

    companion object {
        /** International DashScope endpoint. Use the Beijing host for mainland accounts. */
        const val BASE_URL = "https://dashscope-intl.aliyuncs.com/compatible-mode/v1/"

        /** Multimodal (vision) model for image understanding. */
        const val VISION_MODEL = "qwen-vl-max"

        /** Text model for synthesising search results into pricing. */
        const val TEXT_MODEL = "qwen-plus"
    }
}
