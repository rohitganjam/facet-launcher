package com.facetlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.iconpack.IconPackRepository
import com.facetlauncher.app.data.model.IconPackFallback
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.domain.IconPickerState
import com.facetlauncher.app.domain.ObserveIconPickerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Settings → Appearance → Icons' style and icon pack picker. The Home preview and Icon Shape stay on [AppearanceSettingsViewModel]. */
@HiltViewModel
class IconsViewModel @Inject constructor(
    observeIconPicker: ObserveIconPickerUseCase,
    private val settingsRepository: SettingsRepository,
    private val iconPackRepository: IconPackRepository,
) : ViewModel() {

    val picker: StateFlow<IconPickerState> = observeIconPicker()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IconPickerState())

    /** A built-in style, which also takes over from an applied icon pack. */
    fun selectBuiltIn(mode: IconRenderMode) {
        viewModelScope.launch {
            settingsRepository.setIconRenderMode(mode)
            settingsRepository.setIconPack(null)
        }
    }

    fun selectPack(packageName: String) {
        viewModelScope.launch { settingsRepository.setIconPack(packageName) }
    }

    fun setPackFallback(fallback: IconPackFallback) {
        viewModelScope.launch { settingsRepository.setIconPackFallback(fallback) }
    }

    fun openPack(packageName: String) = iconPackRepository.open(packageName)
}
