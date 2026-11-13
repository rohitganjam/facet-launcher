package com.facetlauncher.app.ui.settings

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.dailyphotos.DailyPhotoFeed
import com.facetlauncher.app.data.dailyphotos.DailyPhotoRepository
import com.facetlauncher.app.data.model.DailyPhoto
import com.facetlauncher.app.data.model.DailyPhotoSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

/** The daily photos screen: a week of photos from one source at a time, and downloading the one the user picks. */
@HiltViewModel
class DailyPhotosViewModel @Inject constructor(
    private val repository: DailyPhotoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyPhotosUiState())
    val uiState: StateFlow<DailyPhotosUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

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

    /** The preview of [photo] from the source on screen. */
    suspend fun thumbnail(photo: DailyPhoto): Bitmap? = repository.thumbnail(_uiState.value.source, photo)
}
