package com.lifelen.core.search.di

import com.lifelen.core.search.SearchClient
import com.lifelen.core.search.serper.SerperApi
import com.lifelen.core.search.serper.SerperSearchClient
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

    @Binds
    @Singleton
    abstract fun bindSearchClient(impl: SerperSearchClient): SearchClient

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
    }
}
