package com.lifelen

import android.app.Application
import com.lifelen.core.data.repository.SettingsRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LifeLensApplication : Application() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // On first launch, seed keys from the build-time secrets so the app works out of the box.
        appScope.launch {
            settingsRepository.seedDefaultsIfEmpty(
                dashScopeKey = BuildConfig.DASHSCOPE_API_KEY,
                searchKey = BuildConfig.SEARCH_API_KEY,
            )
        }
    }
}
