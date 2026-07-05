package com.lifelen.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifelen.core.data.repository.AppSettings
import com.lifelen.core.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    fun setDashScopeKey(value: String) = viewModelScope.launch {
        settingsRepository.setDashScopeApiKey(value)
    }

    fun setSearchKey(value: String) = viewModelScope.launch {
        settingsRepository.setSearchApiKey(value)
    }

    fun setPricingEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setPricingEnabled(enabled)
    }

    fun setDarkTheme(enabled: Boolean?) = viewModelScope.launch {
        settingsRepository.setDarkTheme(enabled)
    }
}
