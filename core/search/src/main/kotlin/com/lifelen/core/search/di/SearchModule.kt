package com.lifelen.core.search.di

import com.lifelen.core.search.AggregatingSearchClient
import com.lifelen.core.search.SearchClient
import com.lifelen.core.search.bing.BingScrapeApi
import com.lifelen.core.search.duckduckgo.DuckDuckGoScrapeApi
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

    // Google first, plus DuckDuckGo, Bing and (when keyed) Serper — see AggregatingSearchClient.
    @Binds
    @Singleton
    abstract fun bindSearchClient(impl: AggregatingSearchClient): SearchClient

    companion object {

        // A self-contained OkHttp client so the search scrapers never inherit the Qwen auth
        // interceptor and its long timeouts.
        private fun scrapeClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        @Provides
        @Singleton
        fun provideSerperApi(): SerperApi {
            val json = Json { ignoreUnknownKeys = true }
            return Retrofit.Builder()
                .baseUrl(SerperApi.BASE_URL)
                .client(scrapeClient())
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(SerperApi::class.java)
        }

        @Provides
        @Singleton
        fun provideGoogleScrapeApi(): GoogleScrapeApi =
            Retrofit.Builder()
                .baseUrl(GoogleScrapeApi.BASE_URL)
                .client(scrapeClient())
                .build()
                .create(GoogleScrapeApi::class.java)

        @Provides
        @Singleton
        fun provideDuckDuckGoScrapeApi(): DuckDuckGoScrapeApi =
            Retrofit.Builder()
                .baseUrl(DuckDuckGoScrapeApi.BASE_URL)
                .client(scrapeClient())
                .build()
                .create(DuckDuckGoScrapeApi::class.java)

        @Provides
        @Singleton
        fun provideBingScrapeApi(): BingScrapeApi =
            Retrofit.Builder()
                .baseUrl(BingScrapeApi.BASE_URL)
                .client(scrapeClient())
                .build()
                .create(BingScrapeApi::class.java)
    }
}
