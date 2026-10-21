package com.facetlauncher.app.ui.facets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListVerticalAlignment
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
import com.facetlauncher.app.domain.ResolvedFacetStyle
import com.facetlauncher.app.domain.resolveFacetStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FacetCarouselUiState(
    val facets: List<Facet> = emptyList(),
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

    /** [facetId]'s overrides resolved against [globalSettings] — what Home shows for that facet, and so what its card previews. */
    fun style(facetId: Long): ResolvedFacetStyle = resolveFacetStyle(facets.find { it.id == facetId }, globalSettings)
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

    fun deleteFacet(facet: Facet) {
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
