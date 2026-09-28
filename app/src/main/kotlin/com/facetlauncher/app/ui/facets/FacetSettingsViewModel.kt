package com.facetlauncher.app.ui.facets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.local.resolveSentinel
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
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.selectedCalendarIds
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FacetSettingsUiState(
    val facet: FacetEntity? = null,
    val activeFacetId: Long = 0L,
    /** This facet's own favorites — only meaningful/used while [isOverridingApps] is true. */
    val favorites: List<PlacedItem> = emptyList(),
    /** The launcher-wide default Favorites list — shown read-only while inheriting. */
    val defaultFavorites: List<PlacedItem> = emptyList(),
    /** This facet's own dock — only meaningful/used while [isOverridingDock] is true. */
    val dockApps: List<PlacedItem> = emptyList(),
    /** The launcher-wide default dock — shown read-only while inheriting, and copied in when the facet switches to Override with an empty list. */
    val defaultDockApps: List<PlacedItem> = emptyList(),
    val globalDockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
    val globalClockTemplateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    val globalClockFontOption: ClockFontOption = ClockFontOption.SYSTEM,
    val globalClockColorOption: ClockColorOption = ClockColorOption.THEME,
    val globalClockAccentColorOption: ClockColorOption = ClockColorOption.ACCENT_PRIMARY,
    val globalUse24HourTime: Boolean = false,
    val globalClockShowMeridiem: Boolean = false,
    val globalClockDateStyle: ClockDateStyle = ClockDateStyle.FULL,
    /** Part of the same Clock+Calendar design bundle as the fields above — see chat history: moved in from being global-only. Also governs the calendar events strip's position, which no longer has an alignment of its own. */
    val globalClockAlignment: ClockAlignment = ClockAlignment.LEFT,
    val globalClockZoneHeightDp: Float? = null,
    val globalAppRowPosition: AppRowPosition = AppRowPosition.LEFT,
    val globalAppRowPresentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    val globalAppListLayout: AppListLayout = AppListLayout.SINGLE_COLUMN,
    val globalAppListColumnAlignment: AppListColumnAlignment = AppListColumnAlignment.BOTH_LEFT,
    val globalAppListGridColumns: AppListGridColumns = AppListGridColumns.FOUR,
    val globalAppListGridDisplayMode: AppListGridDisplayMode = AppListGridDisplayMode.ICONS,
    val globalListContentMode: ListContentMode = ListContentMode.FAVORITES,
    val globalAppsToShowCount: Int = 5,
    val globalShowAllDayEvents: Boolean = true,
    /** `null` means nothing has been explicitly chosen yet — see [com.facetlauncher.app.data.model.LauncherSettings.selectedCalendarIds]. */
    val globalSelectedCalendarIds: Set<String>? = null,
) {
    /** Gates the header's "Apply facet" button — see [FacetSettingsHeader]'s own doc. */
    val isActive: Boolean get() = facet?.id == activeFacetId

    /** The Clock card's single inherit/override switch — governs template, font, color, 24h, and meridiem. */
    val isOverridingClock: Boolean get() = facet?.overrideClock ?: false
    val clockTemplateId: ClockTemplateId get() = if (isOverridingClock) facet?.clockTemplateId ?: globalClockTemplateId else globalClockTemplateId
    val clockFontOption: ClockFontOption get() = if (isOverridingClock) facet?.clockFontOption ?: globalClockFontOption else globalClockFontOption
    val clockColorOption: ClockColorOption get() = if (isOverridingClock) facet?.clockColorOption ?: globalClockColorOption else globalClockColorOption
    val clockAccentColorOption: ClockColorOption get() = if (isOverridingClock) facet?.clockAccentColorOption ?: globalClockAccentColorOption else globalClockAccentColorOption
    val effectiveUse24HourTime: Boolean get() = if (isOverridingClock) facet?.use24HourTime ?: globalUse24HourTime else globalUse24HourTime
    val clockShowMeridiem: Boolean get() = if (isOverridingClock) facet?.clockShowMeridiem ?: globalClockShowMeridiem else globalClockShowMeridiem
    val clockDateStyle: ClockDateStyle get() = if (isOverridingClock) facet?.clockDateStyle ?: globalClockDateStyle else globalClockDateStyle
    val clockAlignment: ClockAlignment get() = if (isOverridingClock) facet?.clockAlignment ?: globalClockAlignment else globalClockAlignment
    val clockZoneHeightDp: Float? get() = if (isOverridingClock) facet?.clockZoneHeightDp ?: globalClockZoneHeightDp else globalClockZoneHeightDp

    /** The Apps card's single inherit/override switch — governs list content mode, apps-to-show, and favorites together now (position/presentation moved to Settings → Appearance — see chat history). */
    val isOverridingApps: Boolean get() = facet?.overrideApps ?: false
    /** Look, not content — edited from Settings → Appearance now, resolved independently of [isOverridingApps] via its own `LAUNCHER_DEFAULT` sentinel. */
    val appRowPosition: AppRowPosition get() = facet.resolveSentinel({ it.appRowPosition }, AppRowPosition.LAUNCHER_DEFAULT, globalAppRowPosition)
    val appRowPresentation: AppRowPresentation get() = facet.resolveSentinel({ it.appRowPresentation }, AppRowPresentation.LAUNCHER_DEFAULT, globalAppRowPresentation)
    val appListLayout: AppListLayout get() = facet.resolveSentinel({ it.appListLayout }, AppListLayout.LAUNCHER_DEFAULT, globalAppListLayout)
    val appListColumnAlignment: AppListColumnAlignment get() = facet.resolveSentinel({ it.appListColumnAlignment }, AppListColumnAlignment.LAUNCHER_DEFAULT, globalAppListColumnAlignment)
    val appListGridColumns: AppListGridColumns get() = facet.resolveSentinel({ it.appListGridColumns }, AppListGridColumns.LAUNCHER_DEFAULT, globalAppListGridColumns)
    val appListGridDisplayMode: AppListGridDisplayMode get() = facet.resolveSentinel({ it.appListGridDisplayMode }, AppListGridDisplayMode.LAUNCHER_DEFAULT, globalAppListGridDisplayMode)
    val listContentMode: ListContentMode get() = if (isOverridingApps) facet?.listContentMode ?: globalListContentMode else globalListContentMode
    val appsToShowCount: Int get() = if (isOverridingApps) facet?.appsToShowCount ?: globalAppsToShowCount else globalAppsToShowCount
    val effectiveFavorites: List<PlacedItem> get() = if (facet?.overridingFavorites == true) favorites else defaultFavorites
    val favoritesLabel: String get() = "${effectiveFavorites.size} of ${FavoriteAppRepository.MAX_FAVORITES}"

    /** The Dock card's single inherit/override switch — governs only the dock's own app list now (Icons/Text display style moved to Settings → Appearance — see chat history). */
    val isOverridingDock: Boolean get() = facet?.overrideDock ?: false
    /** Look, not content — see [appRowPosition]'s own doc for the resolution shape. */
    val dockDisplayMode: DockDisplayMode get() = facet.resolveSentinel({ it.dockDisplayMode }, DockDisplayMode.LAUNCHER_DEFAULT, globalDockDisplayMode)
    val effectiveDockApps: List<PlacedItem> get() = if (isOverridingDock) dockApps else defaultDockApps
    val dockLabel: String get() = "${effectiveDockApps.size} of ${DockAppRepository.MAX_APPS}"

    /** Calendar *selection* has its own override flag, independent of [isOverridingClock] — see `CalendarSettingsUiState`'s own doc comment. */
    val isOverridingCalendar: Boolean get() = facet?.overrideCalendar ?: false
    val effectiveShowAllDayEvents: Boolean get() = if (isOverridingCalendar) facet?.showAllDayEvents ?: globalShowAllDayEvents else globalShowAllDayEvents
    val effectiveSelectedCalendarIds: Set<String>? get() = if (isOverridingCalendar) facet?.selectedCalendarIds ?: globalSelectedCalendarIds else globalSelectedCalendarIds
    /** `null` (nothing explicitly chosen yet) counts as zero here — it isn't the same as "every calendar", it's "none decided". */
    val selectedCalendarCount: Int get() = effectiveSelectedCalendarIds?.size ?: 0
}

@HiltViewModel
class FacetSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val facetRepository: FacetRepository,
    private val settingsRepository: SettingsRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val facetDockAppRepository: FacetDockAppRepository,
    private val dockAppRepository: DockAppRepository,
) : ViewModel() {

    val facetId: Long = checkNotNull(savedStateHandle["facetId"])

    val uiState: StateFlow<FacetSettingsUiState> = combine(
        facetRepository.observeFacets(),
        settingsRepository.settings,
        combine(
            favoriteAppRepository.observeFavoriteItems(facetId),
            defaultFavoriteAppRepository.observeDefaultItems(),
            ::Pair,
        ),
        combine(
            facetDockAppRepository.observeDockItems(facetId),
            dockAppRepository.observeDockItems(),
            ::Pair,
        ),
    ) { facets, settings, favs, docks ->
        val (favorites, defaultFavorites) = favs
        val (dockApps, defaultDockApps) = docks
        FacetSettingsUiState(
            facet = facets.find { it.id == facetId },
            activeFacetId = settings.activeFacetId,
            favorites = favorites,
            defaultFavorites = defaultFavorites,
            dockApps = dockApps,
            defaultDockApps = defaultDockApps,
            globalDockDisplayMode = settings.dockDisplayMode,
            globalClockTemplateId = settings.clockTemplateId,
            globalClockFontOption = settings.clockFontOption,
            globalClockColorOption = settings.clockColorOption,
            globalClockAccentColorOption = settings.clockAccentColorOption,
            globalUse24HourTime = settings.use24HourTime,
            globalClockShowMeridiem = settings.clockShowMeridiem,
            globalClockDateStyle = settings.clockDateStyle,
            globalClockAlignment = settings.clockAlignment,
            globalClockZoneHeightDp = settings.clockZoneHeightDp,
            globalAppRowPosition = settings.appRowPosition,
            globalAppRowPresentation = settings.appRowPresentation,
            globalAppListLayout = settings.appListLayout,
            globalAppListColumnAlignment = settings.appListColumnAlignment,
            globalAppListGridColumns = settings.appListGridColumns,
            globalAppListGridDisplayMode = settings.appListGridDisplayMode,
            globalListContentMode = settings.listContentMode,
            globalAppsToShowCount = settings.appsToShowCount,
            globalShowAllDayEvents = settings.showAllDayEvents,
            globalSelectedCalendarIds = settings.selectedCalendarIds,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FacetSettingsUiState())

    fun renameFacet(newName: String) {
        val facet = uiState.value.facet ?: return
        viewModelScope.launch { facetRepository.renameFacet(facet, newName) }
    }

    /** The header's "Apply facet" button — mirrors `ManageFacetsViewModel.applyFacet`. A no-op if already active (button is disabled in that state, but guard here too since it's cheap). */
    fun applyFacet() {
        if (uiState.value.isActive) return
        viewModelScope.launch { settingsRepository.setActiveFacetId(facetId) }
    }
}
