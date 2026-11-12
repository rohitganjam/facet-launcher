package com.facetlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Whether choosing a glass background should offer to add a wallpaper copy: glass has no usable copy (none stored, or the wallpaper
 * changed since) and the user has not said "Not now" before.
 */
@HiltViewModel
class GlassBackgroundPromptViewModel @Inject constructor(
    private val wallpaperCopyRepository: WallpaperCopyRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    /** Bumped by [onResumed] so the check reads the system wallpaper's id again; it is also read whenever the copy or its recorded id changes. */
    private val resumes = MutableStateFlow(0)

    val needsCopy: StateFlow<Boolean> = combine(wallpaperCopyRepository.copy, settingsRepository.settings, resumes) { copy, settings, _ ->
        val storedId = settings.wallpaperCopySystemId
        val systemId = wallpaperCopyRepository.currentSystemWallpaperId()
        val usable = copy != null && (storedId == WallpaperCopyRepository.NO_WALLPAPER_ID || storedId == systemId)
        !usable && !settings.wallpaperPromptDismissed
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Re-reads the system wallpaper id; call when the launcher comes back, since the wallpaper can change meanwhile. */
    fun onResumed() { resumes.value++ }

    /** "Not now": remembered, so the prompt is not shown again. */
    fun decline() {
        viewModelScope.launch { settingsRepository.setWallpaperPromptDismissed(true) }
    }
}
