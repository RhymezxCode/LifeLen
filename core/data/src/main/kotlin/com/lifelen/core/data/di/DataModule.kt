package com.lifelen.core.data.di

import com.lifelen.core.common.location.RegionProvider
import com.lifelen.core.common.network.ApiKeyProvider
import com.lifelen.core.data.connectivity.AndroidNetworkMonitor
import com.lifelen.core.data.connectivity.NetworkMonitor
import com.lifelen.core.data.location.AndroidRegionProvider
import com.lifelen.core.data.handler.BookHandler
import com.lifelen.core.data.handler.CategoryHandler
import com.lifelen.core.data.handler.ClothingHandler
import com.lifelen.core.data.handler.DocumentHandler
import com.lifelen.core.data.handler.ElectronicsHandler
import com.lifelen.core.data.handler.FoodHandler
import com.lifelen.core.data.handler.GenericHandler
import com.lifelen.core.data.handler.PlantHandler
import com.lifelen.core.data.repository.DataStoreApiKeyProvider
import com.lifelen.core.data.repository.DefaultHistoryRepository
import com.lifelen.core.data.repository.DefaultScanRepository
import com.lifelen.core.data.repository.DefaultSettingsRepository
import com.lifelen.core.data.repository.HistoryRepository
import com.lifelen.core.data.repository.ScanRepository
import com.lifelen.core.data.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindScanRepository(impl: DefaultScanRepository): ScanRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: DefaultHistoryRepository): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: DefaultSettingsRepository): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindApiKeyProvider(impl: DataStoreApiKeyProvider): ApiKeyProvider

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(impl: AndroidNetworkMonitor): NetworkMonitor

    @Binds
    @Singleton
    abstract fun bindRegionProvider(impl: AndroidRegionProvider): RegionProvider

    // --- Object-type handler registry (add a @Binds @IntoSet line for each new type) ---

    @Binds
    @IntoSet
    abstract fun bindGenericHandler(handler: GenericHandler): CategoryHandler

    @Binds
    @IntoSet
    abstract fun bindFoodHandler(handler: FoodHandler): CategoryHandler

    @Binds
    @IntoSet
    abstract fun bindPlantHandler(handler: PlantHandler): CategoryHandler

    @Binds
    @IntoSet
    abstract fun bindDocumentHandler(handler: DocumentHandler): CategoryHandler

    @Binds
    @IntoSet
    abstract fun bindElectronicsHandler(handler: ElectronicsHandler): CategoryHandler

    @Binds
    @IntoSet
    abstract fun bindBookHandler(handler: BookHandler): CategoryHandler

    @Binds
    @IntoSet
    abstract fun bindClothingHandler(handler: ClothingHandler): CategoryHandler
}
