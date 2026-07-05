package com.lifelen.core.search.di

import com.lifelen.core.search.FallbackSearchClient
import com.lifelen.core.search.SearchClient
import com.lifelen.core.search.google.GoogleScrapeApi
import com.lifelen.core.search.serper.SerperApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SearchModule {

    // Serper when a key is configured, else a keyless Google scrape (see FallbackSearchClient).
    @Binds
    @Singleton
    abstract fun bindSearchClient(impl: FallbackSearchClient): SearchClient

    companion object {

        @Provides
        @Singleton
        fun provideSerperApi(): SerperApi {
            // Self-contained client so it never inherits the Qwen auth interceptor.
            val json = Json { ignoreUnknownKeys = true }
            val client = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl(SerperApi.BASE_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(SerperApi::class.java)
        }

        @Provides
        @Singleton
        fun provideGoogleScrapeApi(): GoogleScrapeApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl(GoogleScrapeApi.BASE_URL)
                .client(client)
                .build()
                .create(GoogleScrapeApi::class.java)
        }
    }
}
