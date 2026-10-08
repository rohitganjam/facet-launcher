package com.facetlauncher.app.ui.facets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.local.resolveOverride
import com.facetlauncher.app.data.local.resolveSentinel
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.model.FacetLimits
import com.facetlauncher.app.domain.ActivateFacetByIdUseCase
import com.facetlauncher.app.domain.AddFacetUseCase
import com.facetlauncher.app.domain.DeleteFacetUseCase
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
    /** False for a free user: facets past the third are disabled and the add page opens the upgrade sheet. */
    val isPro: Boolean = true,
) {
    val canAddFacet: Boolean get() = FacetLimits.canAdd(facets.size, isPro)

    /** The add page shows at the limit for a free user too, with a Pro pill, so the way up is visible. */
    val showAddFacet: Boolean get() = canAddFacet || !isPro

    /** The facets the user may switch to or edit; the rest are disabled on the free plan. */
    val selectableFacetIds: Set<Long> get() = FacetLimits.selectableIds(facets.map { it.id }, isPro)
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

    fun clockZoneHeightDp(facetId: Long): Float? =
        facet(facetId).resolveOverride({ it.overrideClock }, { it.clockZoneHeightDp }, globalSettings.clockZoneHeightDp)

    fun clockScale(facetId: Long): Float =
        facet(facetId).resolveOverride({ it.overrideClock }, { it.clockScale }, globalSettings.clockScale)

    /** Mirrors [com.facetlauncher.app.ui.home.HomeUiState.activeAppRowPosition]'s own resolution — a look field, resolved via its own `LAUNCHER_DEFAULT` sentinel independently of `overrideApps`. */
    fun appRowPosition(facetId: Long): AppRowPosition =
        facet(facetId).resolveSentinel({ it.appRowPosition }, AppRowPosition.LAUNCHER_DEFAULT, globalSettings.appRowPosition)

    fun appRowPresentation(facetId: Long): AppRowPresentation =
        facet(facetId).resolveSentinel({ it.appRowPresentation }, AppRowPresentation.LAUNCHER_DEFAULT, globalSettings.appRowPresentation)

    fun appListLayout(facetId: Long): AppListLayout =
        facet(facetId).resolveSentinel({ it.appListLayout }, AppListLayout.LAUNCHER_DEFAULT, globalSettings.appListLayout)

    fun appListColumnAlignment(facetId: Long): AppListColumnAlignment =
        facet(facetId).resolveSentinel({ it.appListColumnAlignment }, AppListColumnAlignment.LAUNCHER_DEFAULT, globalSettings.appListColumnAlignment)

    fun appListGridColumns(facetId: Long): AppListGridColumns =
        facet(facetId).resolveSentinel({ it.appListGridColumns }, AppListGridColumns.LAUNCHER_DEFAULT, globalSettings.appListGridColumns)

    fun appListGridDisplayMode(facetId: Long): AppListGridDisplayMode =
        facet(facetId).resolveSentinel({ it.appListGridDisplayMode }, AppListGridDisplayMode.LAUNCHER_DEFAULT, globalSettings.appListGridDisplayMode)

    fun listContentMode(facetId: Long): ListContentMode =
        facet(facetId)?.let { if (it.overrideApps) it.listContentMode else globalSettings.listContentMode } ?: globalSettings.listContentMode

    /** Mirrors [com.facetlauncher.app.ui.home.HomeUiState.activeDockDisplayMode]'s own resolution — a look field, resolved via its own `LAUNCHER_DEFAULT` sentinel independently of `overrideDock`. The dock's app list itself comes from each facet's [FacetPreviewData.dockApps]. */
    fun dockDisplayMode(facetId: Long): DockDisplayMode =
        facet(facetId).resolveSentinel({ it.dockDisplayMode }, DockDisplayMode.LAUNCHER_DEFAULT, globalSettings.dockDisplayMode)
}

@HiltViewModel
class FacetCarouselViewModel @Inject constructor(
    private val facetRepository: FacetRepository,
    private val settingsRepository: SettingsRepository,
    private val wallpaperRepository: WallpaperRepository,
    private val deleteFacetUseCase: DeleteFacetUseCase,
    observeFacetPreviews: ObserveFacetPreviewsUseCase,
    private val activateFacetById: ActivateFacetByIdUseCase,
    private val addFacetUseCase: AddFacetUseCase,
    entitlementRepository: EntitlementRepository,
) : ViewModel() {

    private val homeWallpaper = MutableStateFlow<HomeWallpaper>(HomeWallpaper.Unavailable)

    val uiState: StateFlow<FacetCarouselUiState> = combine(
        facetRepository.observeFacets(),
        settingsRepository.settings,
        observeFacetPreviews(),
        homeWallpaper,
        entitlementRepository.isPro,
    ) { facets, settings, previewsByFacetId, wallpaper, isPro ->
        FacetCarouselUiState(
            facets = facets,
            activeFacetId = settings.activeFacetId,
            previewsByFacetId = previewsByFacetId,
            globalSettings = settings,
            homeWallpaper = wallpaper,
            isPro = isPro,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FacetCarouselUiState())

    init {
        viewModelScope.launch { homeWallpaper.value = wallpaperRepository.currentHomeWallpaper() }
    }

    /** Applies the given facet as active — the carousel's browse position never persists on its own. */
    fun selectFacet(facetId: Long) {
        viewModelScope.launch { activateFacetById(facetId) }
    }

    fun addFacet() {
        viewModelScope.launch { addFacetUseCase() }
    }

    fun deleteFacet(facet: FacetEntity) {
        if (!uiState.value.canDeleteFacet) return
        viewModelScope.launch {
            deleteFacetUseCase(facet)
            if (uiState.value.activeFacetId == facet.id) {
                // The next facet the user may actually use, judged on the list as it is after this delete.
                val remaining = uiState.value.facets.filter { it.id != facet.id }
                val usable = FacetLimits.selectableIds(remaining.map { it.id }, uiState.value.isPro)
                remaining.firstOrNull { it.id in usable }?.let { settingsRepository.setActiveFacetId(it.id) }
            }
        }
    }
}
