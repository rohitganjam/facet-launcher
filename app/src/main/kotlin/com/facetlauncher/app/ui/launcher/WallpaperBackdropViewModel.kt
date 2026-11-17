package com.facetlauncher.app.ui.launcher

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.WallpaperPaletteRepository
import com.facetlauncher.app.data.model.WallpaperPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * The wallpaper copy the activity draws behind its content for real glass; `null` while there is none, or once the system
 * wallpaper has changed since it was stored (glass then falls back to the tint rather than showing a different image).
 * Also the wallpaper's Material You [palette], computed from that copy, which the theme's accent colours follow.
 */
@HiltViewModel
class WallpaperBackdropViewModel @Inject constructor(
    private val wallpaperCopyRepository: WallpaperCopyRepository,
    settingsRepository: SettingsRepository,
    private val wallpaperPaletteRepository: WallpaperPaletteRepository,
) : ViewModel() {
    /** Bumped by [onResumed] so the check reads the system wallpaper's id again; it is also read whenever the copy or its recorded id changes. */
    private val resumes = MutableStateFlow(0)

    val copy: StateFlow<Bitmap?> = combine(wallpaperCopyRepository.copy, wallpaperCopyRepository.copyIsCurrent, settingsRepository.settings, resumes) { copy, current, settings, _ ->
        val storedId = settings.wallpaperCopySystemId
        val systemId = wallpaperCopyRepository.currentSystemWallpaperId()
        copy.takeIf { current && (storedId == WallpaperCopyRepository.NO_WALLPAPER_ID || storedId == systemId) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** `null` while there is nothing to compute from: the theme then uses the system's Material You. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val palette: StateFlow<WallpaperPalette?> = combine(copy, wallpaperPaletteRepository.systemColorChanges.onStart { emit(Unit) }) { copy, _ -> copy }
        .mapLatest { wallpaperPaletteRepository.paletteFor(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Re-reads the system wallpaper id, since it can change while the launcher is in the background. */
    fun onResumed() {
        wallpaperCopyRepository.recheck()
        resumes.value++
    }
}
