package com.lifelen.core.search.serper

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/** Retrofit binding for Serper.dev's Google Shopping endpoint. */
interface SerperApi {

    @POST("shopping")
    suspend fun shopping(
        @Header("X-API-KEY") apiKey: String,
        @Body body: SerperQuery,
    ): SerperShoppingResponse

    companion object {
        const val BASE_URL = "https://google.serper.dev/"
    }
}

@Serializable
data class SerperQuery(
    val q: String,
    val num: Int = 10,
)

@Serializable
data class SerperShoppingResponse(
    val shopping: List<SerperShoppingItem> = emptyList(),
)

@Serializable
data class SerperShoppingItem(
    val title: String = "",
    val source: String = "",
    val link: String = "",
    val price: String? = null,
)
