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
        // The build-time default keys are supplied to the network layer via DefaultApiKeys (a
        // fallback) — never persisted. This migration clears any default that older builds had
        // seeded into storage, so it no longer shows up in Settings.
        appScope.launch {
            settingsRepository.reconcileDefaultKeys(
                dashScopeKey = BuildConfig.DASHSCOPE_API_KEY,
                searchKey = BuildConfig.SEARCH_API_KEY,
            )
        }
    }
}
