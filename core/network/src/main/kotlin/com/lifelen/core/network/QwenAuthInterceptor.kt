package com.lifelen.core.network

import com.lifelen.core.common.network.ApiKeyProvider
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** Attaches the DashScope bearer token to every Qwen request, fetched fresh from storage. */
class QwenAuthInterceptor @Inject constructor(
    private val apiKeyProvider: ApiKeyProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val key = runBlocking { apiKeyProvider.dashScopeApiKey() }
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $key")
            .addHeader("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}
