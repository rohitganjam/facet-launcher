package com.facetlauncher.app.ui.facets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.local.resolveOverride
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.domain.ObserveFacetPreviewsUseCase
import com.facetlauncher.app.domain.FacetPreviewData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FacetCarouselUiState(
    val facets: List<FacetEntity> = emptyList(),
    val activeFacetId: Long = 0L,
    val previewsByFacetId: Map<Long, FacetPreviewData> = emptyMap(),
    val globalSettings: LauncherSettings = LauncherSettings(),
    // The live system wallpaper, rendered behind each preview card (Home's own window-level
    // wallpaper compositing can't reach a card). Loaded once when the carousel opens.
    val homeWallpaper: HomeWallpaper = HomeWallpaper.Unavailable,
) {
    val canAddFacet: Boolean get() = facets.size < FacetRepository.MAX_FACETS
    val canDeleteFacet: Boolean get() = facets.size > FacetRepository.MIN_FACETS

    private fun facet(id: Long) = facets.find { it.id == id }

    fun clockTemplateId(facetId: Long): ClockTemplateId =
        facet(facetId)?.let { if (it.overrideClock) it.clockTemplateId else globalSettings.clockTemplateId } ?: globalSettings.clockTemplateId

    fun clockFontOption(facetId: Long): ClockFontOption =
        facet(facetId)?.let { if (it.overrideClock) it.clockFontOption else globalSettings.clockFontOption } ?: globalSettings.clockFontOption

    fun clockColorOption(facetId: Long): ClockColorOption =
        facet(facetId)?.let { if (it.overrideClock) it.clockColorOption else globalSettings.clockColorOption } ?: globalSettings.clockColorOption

    /** See [com.facetlauncher.app.data.model.LauncherSettings.clockAccentColorOption]. */
    fun clockAccentColorOption(facetId: Long): ClockColorOption =
        facet(facetId)?.let { if (it.overrideClock) it.clockAccentColorOption else globalSettings.clockAccentColorOption } ?: globalSettings.clockAccentColorOption

    fun effectiveUse24HourTime(facetId: Long): Boolean =
        facet(facetId)?.let { if (it.overrideClock) it.use24HourTime else globalSettings.use24HourTime } ?: globalSettings.use24HourTime

    fun clockShowMeridiem(facetId: Long): Boolean =
        facet(facetId)?.let { if (it.overrideClock) it.clockShowMeridiem else globalSettings.clockShowMeridiem } ?: globalSettings.clockShowMeridiem

    /** See [com.facetlauncher.app.data.model.LauncherSettings.clockDateStyle]. */
    fun clockDateStyle(facetId: Long): ClockDateStyle =
        facet(facetId)?.let { if (it.overrideClock) it.clockDateStyle else globalSettings.clockDateStyle } ?: globalSettings.clockDateStyle

    fun clockAlignment(facetId: Long): ClockAlignment =
        facet(facetId).resolveOverride({ it.overrideClock }, { it.clockAlignment }, globalSettings.clockAlignment)

    /** Independent of [clockAlignment] — see [com.facetlauncher.app.data.model.LauncherSettings.calendarAlignment]. */
    fun calendarAlignment(facetId: Long): ClockAlignment =
        facet(facetId).resolveOverride({ it.overrideClock }, { it.calendarAlignment }, globalSettings.calendarAlignment)

    fun clockZoneHeightDp(facetId: Long): Float? =
        facet(facetId).resolveOverride({ it.overrideClock }, { it.clockZoneHeightDp }, globalSettings.clockZoneHeightDp)

    fun clockScale(facetId: Long): Float =
        facet(facetId).resolveOverride({ it.overrideClock }, { it.clockScale }, globalSettings.clockScale)

    /** Mirrors [com.facetlauncher.app.ui.home.HomeUiState.activeCalendarFontOption]'s own gating exactly — same `overrideCalendar` flag. */
    fun calendarFontOption(facetId: Long): ClockFontOption =
        facet(facetId)?.let { if (it.overrideCalendar) it.calendarFontOption else globalSettings.calendarFontOption } ?: globalSettings.calendarFontOption

    fun calendarColorOption(facetId: Long): ClockColorOption =
        facet(facetId)?.let { if (it.overrideCalendar) it.calendarColorOption else globalSettings.calendarColorOption } ?: globalSettings.calendarColorOption

    fun calendarFontWeight(facetId: Long): FontWeightOption =
        facet(facetId)?.let { if (it.overrideCalendar) it.calendarFontWeight else globalSettings.calendarFontWeight } ?: globalSettings.calendarFontWeight

    /** Mirrors [com.facetlauncher.app.ui.home.HomeUiState.activeAppRowPosition]'s own gating exactly — same `overrideApps` flag. */
    fun appRowPosition(facetId: Long): AppRowPosition =
        facet(facetId)?.let { if (it.overrideApps) it.appRowPosition else globalSettings.appRowPosition } ?: globalSettings.appRowPosition

    fun appRowPresentation(facetId: Long): AppRowPresentation =
        facet(facetId)?.let { if (it.overrideApps) it.appRowPresentation else globalSettings.appRowPresentation } ?: globalSettings.appRowPresentation

    fun listContentMode(facetId: Long): ListContentMode =
        facet(facetId)?.let { if (it.overrideApps) it.listContentMode else globalSettings.listContentMode } ?: globalSettings.listContentMode

    /** Mirrors [com.facetlauncher.app.ui.home.HomeUiState.activeDockDisplayMode]'s own gating — same `overrideDock` flag. The dock's app list itself comes from each facet's [FacetPreviewData.dockApps]. */
    fun dockDisplayMode(facetId: Long): DockDisplayMode =
        facet(facetId)?.let { if (it.overrideDock) it.dockDisplayMode else globalSettings.dockDisplayMode } ?: globalSettings.dockDisplayMode
}

@HiltViewModel
class FacetCarouselViewModel @Inject constructor(
    private val facetRepository: FacetRepository,
    private val settingsRepository: SettingsRepository,
    private val wallpaperRepository: WallpaperRepository,
    observeFacetPreviews: ObserveFacetPreviewsUseCase,
) : ViewModel() {

    private val homeWallpaper = MutableStateFlow<HomeWallpaper>(HomeWallpaper.Unavailable)

    val uiState: StateFlow<FacetCarouselUiState> = combine(
        facetRepository.observeFacets(),
        settingsRepository.settings,
        observeFacetPreviews(),
        homeWallpaper,
    ) { facets, settings, previewsByFacetId, wallpaper ->
        FacetCarouselUiState(
            facets = facets,
            activeFacetId = settings.activeFacetId,
            previewsByFacetId = previewsByFacetId,
            globalSettings = settings,
            homeWallpaper = wallpaper,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FacetCarouselUiState())

    init {
        viewModelScope.launch { homeWallpaper.value = wallpaperRepository.currentHomeWallpaper() }
    }

    /** Applies the given facet as active — the carousel's browse position never persists on its own. */
    fun selectFacet(facetId: Long) {
        viewModelScope.launch { settingsRepository.setActiveFacetId(facetId) }
    }

    fun addFacet() {
        if (!uiState.value.canAddFacet) return
        viewModelScope.launch { facetRepository.addFacet() }
    }

    fun deleteFacet(facet: FacetEntity) {
        if (!uiState.value.canDeleteFacet) return
        viewModelScope.launch {
            facetRepository.deleteFacet(facet)
            if (uiState.value.activeFacetId == facet.id) {
                val remaining = uiState.value.facets.firstOrNull { it.id != facet.id }
                remaining?.let { settingsRepository.setActiveFacetId(it.id) }
            }
        }
    }
}
