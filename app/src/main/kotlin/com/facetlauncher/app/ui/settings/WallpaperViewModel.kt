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
import com.facetlauncher.app.data.dailyphotos.DailyWallpaperRepository
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.OtherScreenPrompt
import com.facetlauncher.app.data.model.ScreenSize
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.facetlauncher.app.domain.NoteManualWallpaperUseCase
import com.facetlauncher.app.domain.ObserveWallpaperScreensUseCase
import com.facetlauncher.app.domain.RemoveWallpaperCopyUseCase
import com.facetlauncher.app.domain.KeptOriginal
import com.facetlauncher.app.domain.SetWallpaperFromPhotoUseCase
import com.facetlauncher.app.domain.SetWallpaperResult
import com.facetlauncher.app.domain.UseScreenshotAsWallpaperCopyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The longest side a picked photo is decoded at: enough to zoom in on, without holding a huge bitmap. */
internal const val MAX_PICKED_SIDE = 3072

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
    /** The wallpaper is a live one, so a copy is only a still frame of it. */
    val liveWallpaper: Boolean = false,
    /** The wallpaper was set but its original could not be kept (storage), so a screen first seen later needs a capture. */
    val originalNotKept: Boolean = false,
    /** `true` after returning from Android's wallpaper picker with a new wallpaper and no matching copy: offer to add one. */
    val offerCopy: Boolean = false,
    /** The credit of the daily photo being framed, shown as it was given; `null` for a photo the user chose. */
    val credit: String? = null,
    /** `true` while the photo being framed is a screenshot of the current wallpaper: it is stored as the copy only, not set as the wallpaper. */
    val copyOnly: Boolean = false,
    /** `true` while framing the user's own image with the daily wallpaper on: setting it turns the daily change off, and the fit step says so. */
    val turnsOffDailyWallpaper: Boolean = false,
    /** Every screen known, the largest first: the fit step frames the first and outlines what the others will show. */
    val fitScreens: ImmutableList<ScreenSize> = persistentListOf(),
    /** A suggestion to add a copy for another screen, or none. */
    val otherScreenPrompt: OtherScreenPrompt = OtherScreenPrompt(),
)

/** The wallpaper screen: the glass copy, the picker and fit step that make one, and removing it. */
@HiltViewModel
class WallpaperViewModel @Inject constructor(
    private val wallpaperCopyRepository: WallpaperCopyRepository,
    private val wallpaperRepository: WallpaperRepository,
    private val settingsRepository: SettingsRepository,
    private val setWallpaperFromPhoto: SetWallpaperFromPhotoUseCase,
    private val removeWallpaperCopy: RemoveWallpaperCopyUseCase,
    private val useScreenshotAsCopy: UseScreenshotAsWallpaperCopyUseCase,
    dailyWallpaperRepository: DailyWallpaperRepository,
    private val noteManualWallpaper: NoteManualWallpaperUseCase,
    private val observeScreens: ObserveWallpaperScreensUseCase,
) : ViewModel() {

    private data class Local(
        val picked: Bitmap? = null,
        /** Where the picked image came from, so its original can be kept; `null` for a daily photo. */
        val pickedUri: Uri? = null,
        /** A daily photo's downloaded file, kept as its original; `null` for anything else. */
        val pickedBytes: ByteArray? = null,
        /** The last wallpaper was set, but its original could not be kept, so a screen first seen later needs a capture. */
        val originalNotKept: Boolean = false,
        val working: Boolean = false,
        val failed: Boolean = false,
        val systemWallpaper: HomeWallpaper = HomeWallpaper.Unavailable,
        val live: Boolean = false,
        /** The system wallpaper's id when Android's picker was opened; `null` when it wasn't. */
        val pickerBaseline: Int? = null,
        val offerCopy: Boolean = false,
        val credit: String? = null,
        val copyOnly: Boolean = false,
        /** The photo being framed is one of the daily photos, not the user's own image. */
        val fromDailyPhoto: Boolean = false,
        /** Bumped by [refresh] so the stale check reads the system wallpaper's id again. */
        val checks: Int = 0,
    )

    private val local = MutableStateFlow(Local())

    val uiState: StateFlow<WallpaperUiState> = combine(combine(wallpaperCopyRepository.copy, wallpaperCopyRepository.copyIsCurrent) { copy, current -> copy to current }, settingsRepository.settings, local, dailyWallpaperRepository.state, observeScreens()) { (copy, current), settings, state, daily, screens ->
        val storedId = settings.wallpaperCopySystemId
        val stale = copy != null && (!current || (storedId != WallpaperCopyRepository.NO_WALLPAPER_ID && storedId != wallpaperCopyRepository.currentSystemWallpaperId()))
        WallpaperUiState(
            copy = copy,
            systemWallpaper = state.systemWallpaper,
            stale = stale,
            picked = state.picked,
            isWorking = state.working,
            failed = state.failed,
            originalNotKept = state.originalNotKept,
            liveWallpaper = state.live,
            // Gone as soon as a matching copy exists, however it got there (the offer's button, the capture row, the pill).
            offerCopy = state.offerCopy && (copy == null || stale),
            credit = state.credit,
            copyOnly = state.copyOnly,
            turnsOffDailyWallpaper = daily.enabled && state.picked != null && !state.copyOnly && !state.fromDailyPhoto,
            fitScreens = screens.sizes,
            otherScreenPrompt = screens.prompt,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), WallpaperUiState())

    init {
        refresh()
    }

    /** Reads the system wallpaper again (a preview of it, and whether the copy still matches); call when the screen comes back. */
    fun refresh() {
        wallpaperCopyRepository.recheck()
        viewModelScope.launch {
            val systemWallpaper = wallpaperRepository.currentHomeWallpaper()
            val checked = local.value.pickerBaseline
            val offer = shouldOfferCopy(checked)
            // Only the starting point just checked is used up: the picker may have opened again while this was in flight.
            local.update { it.copy(systemWallpaper = systemWallpaper, live = wallpaperCopyRepository.isLiveWallpaper(), checks = it.checks + 1, pickerBaseline = it.pickerBaseline.takeUnless { current -> current == checked }, offerCopy = it.offerCopy || offer) }
        }
    }

    /** Android's picker is about to open: remember the wallpaper it starts from, so coming back can tell whether it changed. */
    fun onSystemPickerOpened() = local.update { it.copy(pickerBaseline = wallpaperCopyRepository.currentSystemWallpaperId()) }

    /** The picker changed the wallpaper and glass has no copy of the new one (none stored, or the stored one is now stale), and the user hasn't said no. */
    private suspend fun shouldOfferCopy(baseline: Int?): Boolean {
        val systemId = wallpaperCopyRepository.currentSystemWallpaperId()
        val settings = settingsRepository.settings.first()
        val copyMatches = wallpaperCopyRepository.copy.first() != null && settings.wallpaperCopySystemId == systemId
        return baseline != null && baseline != systemId && !copyMatches && !settings.wallpaperPromptDismissed
    }

    /** "Not now" on the suggestion about another screen: remembered, so it is not shown again. */
    fun dismissOtherScreenPrompt() {
        val keys = uiState.value.otherScreenPrompt.dismissKeys
        viewModelScope.launch { keys.forEach { wallpaperCopyRepository.dismissPrompt(it) } }
    }

    /** "Not now" on the offer: remembered, so it isn't shown again. */
    fun declineOffer() {
        local.update { it.copy(offerCopy = false) }
        viewModelScope.launch { settingsRepository.setWallpaperPromptDismissed(true) }
    }

    fun clearOffer() = local.update { it.copy(offerCopy = false) }

    /** The photo picker is about to open: [copyOnly] is `true` for a screenshot of the current wallpaper, `false` for an image to set as it. */
    fun onPickStarted(copyOnly: Boolean) = local.update { it.copy(copyOnly = copyOnly) }

    /** A photo was picked: decode it for the fit step. */
    fun onPicked(uri: Uri) {
        viewModelScope.launch {
            local.update { it.copy(working = true, failed = false, originalNotKept = false) }
            val photo = wallpaperCopyRepository.decodePicked(uri, MAX_PICKED_SIDE)
            local.update { it.copy(picked = photo, pickedUri = uri, pickedBytes = null, credit = null, fromDailyPhoto = false, working = false, failed = photo == null) }
        }
    }

    /** A daily photo was downloaded: frame it, with its [credit] shown on it. */
    fun onPickedBitmap(photo: Bitmap, credit: String, original: ByteArray? = null) = local.update { it.copy(picked = photo, pickedUri = null, pickedBytes = original, credit = credit.ifBlank { null }, copyOnly = false, fromDailyPhoto = true, failed = false) }

    fun cancelFit() = local.update { it.copy(picked = null, pickedUri = null, pickedBytes = null, credit = null, failed = false) }

    /** The user framed [visible] of the picked photo: set it as the wallpaper, store what each screen shows of it as the glass copies, and keep the original. */
    fun confirmFit(visible: RectF, includeLock: Boolean) {
        val photo = local.value.picked ?: return
        viewModelScope.launch {
            local.update { it.copy(working = true, failed = false) }
            val copyOnly = local.value.copyOnly
            // The image is kept as it is (a daily photo's download too); a screenshot of the wallpaper is not.
            val original = if (copyOnly) null else local.value.pickedBytes?.let { KeptOriginal.Bytes(it) } ?: local.value.pickedUri?.let { KeptOriginal.Picked(it) }
            val result = if (copyOnly) SetWallpaperResult(useScreenshotAsCopy(photo, visible)) else setWallpaperFromPhoto(photo, visible, includeLock, original)
            val done = result.set
            if (done && !copyOnly) noteManualWallpaper(fromDailyPhoto = local.value.fromDailyPhoto)
            local.update { it.copy(picked = if (done) null else it.picked, pickedUri = if (done) null else it.pickedUri, pickedBytes = if (done) null else it.pickedBytes, credit = if (done) null else it.credit, working = false, failed = !done, originalNotKept = done && !result.originalKept) }
        }
    }

    fun removeCopy() {
        local.update { it.copy(originalNotKept = false) }
        viewModelScope.launch { removeWallpaperCopy() }
    }

    fun dismissFailure() = local.update { it.copy(failed = false) }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
