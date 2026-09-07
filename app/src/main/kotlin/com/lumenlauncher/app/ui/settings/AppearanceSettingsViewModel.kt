package com.lumenlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.data.model.IconRenderMode
import com.lumenlauncher.app.data.model.LauncherFontOption
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.data.model.ThemeMode
import com.lumenlauncher.app.data.model.WallpaperAccentRole
import com.lumenlauncher.app.ui.theme.AccentSwatch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Settings → Appearance (`3c`'s theme/accent/icons/font block, split into its own screen — see
 * chat history: the main Settings list was getting too long to scan). Every field here is global
 * only — no per-profile override exists for these, unlike Clock/Calendar/Apps.
 */
@HiltViewModel
class AppearanceSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<LauncherSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LauncherSettings())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setAccentFromSystem(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAccentFromSystem(enabled) }
    }

    fun setCustomAccentSwatch(swatch: AccentSwatch) {
        viewModelScope.launch { settingsRepository.setCustomAccentSwatch(swatch.name) }
    }

    fun setWallpaperAccentRole(role: WallpaperAccentRole) {
        viewModelScope.launch { settingsRepository.setWallpaperAccentRole(role) }
    }

    fun setIconRenderMode(mode: IconRenderMode) {
        viewModelScope.launch { settingsRepository.setIconRenderMode(mode) }
    }

    fun setLauncherFontOption(option: LauncherFontOption) {
        viewModelScope.launch { settingsRepository.setLauncherFontOption(option) }
    }

    fun setAppLabelColorOption(option: ClockColorOption) {
        viewModelScope.launch { settingsRepository.setAppLabelColorOption(option) }
    }

    fun setHomeAppsFontWeight(weight: FontWeightOption) {
        viewModelScope.launch { settingsRepository.setHomeAppsFontWeight(weight) }
    }
}
