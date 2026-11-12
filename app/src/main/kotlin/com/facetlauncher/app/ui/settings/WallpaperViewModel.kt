package com.facetlauncher.app.ui.settings

import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.domain.RemoveWallpaperCopyUseCase
import com.facetlauncher.app.domain.SetWallpaperFromPhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The longest side a picked photo is decoded at: enough to zoom in on, without holding a huge bitmap. */
private const val MAX_PICKED_SIDE = 3072

/**
 * What the wallpaper screen shows. [copy] is the stored glass copy; [stale] is `true` when the system wallpaper has changed since it was
 * stored; [picked] is a photo being framed in the fit step; [failed] is set after a photo could not be read or the wallpaper could not be set.
 */
@Immutable
data class WallpaperUiState(
    val copy: Bitmap? = null,
    val systemWallpaper: HomeWallpaper = HomeWallpaper.Unavailable,
    val stale: Boolean = false,
    val picked: Bitmap? = null,
    val isWorking: Boolean = false,
    val failed: Boolean = false,
)

/** The wallpaper screen: the glass copy, the picker and fit step that make one, and removing it. */
@HiltViewModel
class WallpaperViewModel @Inject constructor(
    private val wallpaperCopyRepository: WallpaperCopyRepository,
    private val wallpaperRepository: WallpaperRepository,
    settingsRepository: SettingsRepository,
    private val setWallpaperFromPhoto: SetWallpaperFromPhotoUseCase,
    private val removeWallpaperCopy: RemoveWallpaperCopyUseCase,
) : ViewModel() {

    private data class Local(
        val picked: Bitmap? = null,
        val working: Boolean = false,
        val failed: Boolean = false,
        val systemWallpaper: HomeWallpaper = HomeWallpaper.Unavailable,
        /** Bumped by [refresh] so the stale check reads the system wallpaper's id again. */
        val checks: Int = 0,
    )

    private val local = MutableStateFlow(Local())

    val uiState: StateFlow<WallpaperUiState> = combine(wallpaperCopyRepository.copy, settingsRepository.settings, local) { copy, settings, state ->
        val storedId = settings.wallpaperCopySystemId
        WallpaperUiState(
            copy = copy,
            systemWallpaper = state.systemWallpaper,
            stale = copy != null && storedId != WallpaperCopyRepository.NO_WALLPAPER_ID && storedId != wallpaperCopyRepository.currentSystemWallpaperId(),
            picked = state.picked,
            isWorking = state.working,
            failed = state.failed,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), WallpaperUiState())

    init {
        refresh()
    }

    /** Reads the system wallpaper again (a preview of it, and whether the copy still matches); call when the screen comes back. */
    fun refresh() {
        viewModelScope.launch {
            val systemWallpaper = wallpaperRepository.currentHomeWallpaper()
            local.update { it.copy(systemWallpaper = systemWallpaper, checks = it.checks + 1) }
        }
    }

    /** A photo was picked: decode it for the fit step. */
    fun onPicked(uri: Uri) {
        viewModelScope.launch {
            local.update { it.copy(working = true, failed = false) }
            val photo = wallpaperCopyRepository.decodePicked(uri, MAX_PICKED_SIDE)
            local.update { it.copy(picked = photo, working = false, failed = photo == null) }
        }
    }

    fun cancelFit() = local.update { it.copy(picked = null, failed = false) }

    /** The user framed [visible] of the picked photo: set it as the wallpaper and store it as the glass copy. */
    fun confirmFit(visible: RectF, includeLock: Boolean) {
        val photo = local.value.picked ?: return
        viewModelScope.launch {
            local.update { it.copy(working = true, failed = false) }
            val done = setWallpaperFromPhoto(photo, visible, includeLock)
            local.update { it.copy(picked = if (done) null else it.picked, working = false, failed = !done) }
        }
    }

    fun removeCopy() {
        viewModelScope.launch { removeWallpaperCopy() }
    }

    fun dismissFailure() = local.update { it.copy(failed = false) }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
