package com.lifelen.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifelen.core.data.repository.AppSettings
import com.lifelen.core.data.repository.HistoryRepository
import com.lifelen.core.data.repository.SettingsRepository
import com.lifelen.core.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    val uiState: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    /** Persists both keys at once (the explicit "Save keys" action). */
    fun saveKeys(dashScopeKey: String, searchKey: String) = viewModelScope.launch {
        settingsRepository.setDashScopeApiKey(dashScopeKey)
        settingsRepository.setSearchApiKey(searchKey)
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { settingsRepository.setThemeMode(mode) }

    fun setPricingEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepository.setPricingEnabled(enabled) }

    fun setHapticsEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepository.setHapticsEnabled(enabled) }

    fun setAutoSaveScans(enabled: Boolean) = viewModelScope.launch { settingsRepository.setAutoSaveScans(enabled) }
    fun setAutoScan(enabled: Boolean) = viewModelScope.launch { settingsRepository.setAutoScan(enabled) }

    fun setRememberKeys(remember: Boolean) = viewModelScope.launch { settingsRepository.setRememberKeys(remember) }

    fun clearLibrary() = viewModelScope.launch { historyRepository.clearAll() }
}
