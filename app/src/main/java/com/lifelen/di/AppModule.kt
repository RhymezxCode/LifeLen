package com.lifelen.di

import com.lifelen.BuildConfig
import com.lifelen.core.common.network.DefaultApiKeys
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** App-level bindings. Exposes the build-time [DefaultApiKeys] (from `secrets.properties`) so the
 * network layer can fall back to them without ever persisting or displaying them. */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDefaultApiKeys(): DefaultApiKeys = DefaultApiKeys(
        dashScopeKey = BuildConfig.DASHSCOPE_API_KEY,
        searchKey = BuildConfig.SEARCH_API_KEY,
    )
}
