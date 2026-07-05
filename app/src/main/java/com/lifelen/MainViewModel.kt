package com.lifelen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifelen.core.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {

    /** null = follow the system setting. */
    val darkTheme: StateFlow<Boolean?> = settingsRepository.settings
        .map { it.darkTheme }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
