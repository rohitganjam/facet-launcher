package com.lumenlauncher.app.ui.home.clock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.LauncherSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Backs the global (persisted, real-Home-affecting) clock style gallery — reached from Settings' Clock card. */
@HiltViewModel
class ClockStyleGalleryViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<LauncherSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LauncherSettings())

    fun setClockTemplateId(id: ClockTemplateId) {
        viewModelScope.launch { settingsRepository.setClockTemplateId(id) }
    }

    fun setClockFontOption(option: ClockFontOption) {
        viewModelScope.launch { settingsRepository.setClockFontOption(option) }
    }

    fun setClockColorOption(option: ClockColorOption) {
        viewModelScope.launch { settingsRepository.setClockColorOption(option) }
    }

    fun setUse24HourTime(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setUse24HourTime(enabled) }
    }

    fun setClockShowMeridiem(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setClockShowMeridiem(enabled) }
    }
}
