package com.lifelen.core.datastore.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.rhymezxcode.simplestore.DatastorePreference
import io.github.rhymezxcode.simplestore.SimpleStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideSimpleStore(@ApplicationContext context: Context): SimpleStore =
        SimpleStore.Builder()
            .context(context)
            .storeName("lifelens_preferences")
            .encryption(false) // SimpleStore notes encrypted DataStore is still in development.
            .build()

    /**
     * Single [DatastorePreference] instance — DataStore requires one reference per file per process,
     * so this must be a singleton rather than calling getType() at each use site.
     */
    @Provides
    @Singleton
    fun provideDatastorePreference(store: SimpleStore): DatastorePreference =
        store.getType<DatastorePreference>()
}
