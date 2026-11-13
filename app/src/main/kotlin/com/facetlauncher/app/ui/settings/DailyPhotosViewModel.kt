package com.facetlauncher.app.ui.settings

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.dailyphotos.DailyPhotoFeed
import com.facetlauncher.app.data.DeviceConditionsRepository
import com.facetlauncher.app.data.dailyphotos.DailyPhotoRepository
import com.facetlauncher.app.data.dailyphotos.DailyWallpaperRepository
import com.facetlauncher.app.data.model.DailyPhoto
import com.facetlauncher.app.data.model.DailyPhotoSource
import com.facetlauncher.app.domain.DailyWallpaperCoordinator
import java.time.Clock
import java.time.LocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A photo the user tapped, downloaded and ready to frame, with its credit. */
@Immutable
data class PickedDailyPhoto(val bitmap: Bitmap, val credit: String)

/**
 * What the daily photos screen shows. [savedEarlier] is set when today's refresh failed and the photos are from an earlier day;
 * [failed] when there is nothing to show; [downloading] while a tapped photo's original comes down; [picked] once it has.
 */
@Immutable
data class DailyPhotosUiState(
    val source: DailyPhotoSource = DailyPhotoSource.BING,
    val photos: ImmutableList<DailyPhoto> = persistentListOf(),
    val loading: Boolean = true,
    val failed: Boolean = false,
    val savedEarlier: Boolean = false,
    val downloading: Boolean = false,
    val downloadFailed: Boolean = false,
    val picked: PickedDailyPhoto? = null,
)

/** The sheet that turns the daily wallpaper on: its two choices, and whether the phone is on mobile data with Wi-Fi only on. */
@Immutable
data class DailyWallpaperSheet(val source: DailyPhotoSource, val includeLock: Boolean, val onMobileData: Boolean)

/** The daily wallpaper settings above the grid. [currentImageUrl] is the photo it last set, marked in the grid. */
@Immutable
data class DailyWallpaperUiState(
    val enabled: Boolean = false,
    val source: DailyPhotoSource = DailyPhotoSource.BING,
    val wifiOnly: Boolean = true,
    val includeLock: Boolean = false,
    val status: DailyWallpaperStatus = DailyWallpaperStatus.Off,
    val currentImageUrl: String? = null,
    val sheet: DailyWallpaperSheet? = null,
)

/** The daily photos screen: a week of photos from one source at a time, downloading the one the user picks, and the daily wallpaper settings. */
@HiltViewModel
class DailyPhotosViewModel @Inject constructor(
    private val repository: DailyPhotoRepository,
    dailyWallpaperRepository: DailyWallpaperRepository,
    private val dailyWallpaperCoordinator: DailyWallpaperCoordinator,
    private val deviceConditions: DeviceConditionsRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyPhotosUiState())
    val uiState: StateFlow<DailyPhotosUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private val sheet = MutableStateFlow<DailyWallpaperSheet?>(null)

    val dailyWallpaper: StateFlow<DailyWallpaperUiState> = combine(dailyWallpaperRepository.state, sheet) { state, sheet ->
        DailyWallpaperUiState(
            enabled = state.enabled,
            source = state.source,
            wifiOnly = state.wifiOnly,
            includeLock = state.includeLock,
            status = dailyWallpaperStatus(state, LocalDate.now(clock), clock.zone),
            currentImageUrl = state.current?.imageUrl?.takeIf { state.enabled },
            sheet = sheet,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DailyWallpaperUiState())

    init {
        load(DailyPhotoSource.BING)
    }

    /** Shows [source]'s photos; choosing the one already shown does nothing unless it failed. */
    fun selectSource(source: DailyPhotoSource) {
        val current = _uiState.value
        if (source != current.source || current.failed) load(source)
    }

    fun retry() = load(_uiState.value.source)

    private fun load(source: DailyPhotoSource) {
        loadJob?.cancel()
        _uiState.update { it.copy(source = source, photos = persistentListOf(), loading = true, failed = false, savedEarlier = false, downloadFailed = false) }
        loadJob = viewModelScope.launch {
            val feed = repository.photos(source)
            _uiState.update {
                when (feed) {
                    is DailyPhotoFeed.Photos -> it.copy(photos = feed.photos.toImmutableList(), loading = false, savedEarlier = feed.savedEarlier)
                    DailyPhotoFeed.Unavailable -> it.copy(loading = false, failed = true)
                }
            }
        }
    }

    /** [photo] was tapped: download its original for the fit step. Ignored while one is already coming down. */
    fun pick(photo: DailyPhoto) {
        if (_uiState.value.downloading) return
        _uiState.update { it.copy(downloading = true, downloadFailed = false) }
        viewModelScope.launch {
            val bitmap = repository.original(photo, MAX_PICKED_SIDE)
            _uiState.update {
                if (bitmap != null) it.copy(downloading = false, picked = PickedDailyPhoto(bitmap, photo.credit)) else it.copy(downloading = false, downloadFailed = true)
            }
        }
    }

    /** The picked photo has been handed on. */
    fun consumePicked() = _uiState.update { it.copy(picked = null) }

    /** The daily wallpaper switch: on opens the sheet (preset to the tab on screen), off turns it off straight away. */
    fun onDailyWallpaperSwitch(on: Boolean) {
        if (!on) {
            sheet.value = null
            dailyWallpaperCoordinator.turnOff()
            return
        }
        val network = deviceConditions.network()
        val onMobileData = dailyWallpaper.value.wifiOnly && network.connected && !network.unmetered
        sheet.value = DailyWallpaperSheet(source = _uiState.value.source, includeLock = dailyWallpaper.value.includeLock, onMobileData = onMobileData)
    }

    fun onSheetSource(source: DailyPhotoSource) = sheet.update { it?.copy(source = source) }

    fun onSheetIncludeLock(includeLock: Boolean) = sheet.update { it?.copy(includeLock = includeLock) }

    fun dismissSheet() {
        sheet.value = null
    }

    /** "Turn on" on the sheet; [useMobileData] downloads today's photo now although the phone is on mobile data. */
    fun confirmTurnOn(useMobileData: Boolean) {
        val choice = sheet.value ?: return
        sheet.value = null
        dailyWallpaperCoordinator.turnOn(choice.source, choice.includeLock, useMobileData)
    }

    fun setDailySource(source: DailyPhotoSource) = dailyWallpaperCoordinator.setSource(source)

    fun setDailyIncludeLock(includeLock: Boolean) = dailyWallpaperCoordinator.setIncludeLock(includeLock)

    fun setDailyWifiOnly(wifiOnly: Boolean) = dailyWallpaperCoordinator.setWifiOnly(wifiOnly)

    fun resumeDailyWallpaper() = dailyWallpaperCoordinator.resume()

    /** The preview of [photo] from the source on screen. */
    suspend fun thumbnail(photo: DailyPhoto): Bitmap? = repository.thumbnail(_uiState.value.source, photo)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
