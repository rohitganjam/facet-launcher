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
import com.facetlauncher.app.data.model.ListContentMode
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
    /** This facet's own favorites — only meaningful/used while [isOverridingApps] is true. */
    val favorites: List<AppInfo> = emptyList(),
    /** The launcher-wide default Favorites list — shown read-only while inheriting. */
    val defaultFavorites: List<AppInfo> = emptyList(),
    /** This facet's own dock — only meaningful/used while [isOverridingDock] is true. */
    val dockApps: List<AppInfo> = emptyList(),
    /** The launcher-wide default dock — shown read-only while inheriting, and copied in when the facet switches to Override with an empty list. */
    val defaultDockApps: List<AppInfo> = emptyList(),
    val globalDockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
    val globalClockTemplateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    val globalClockFontOption: ClockFontOption = ClockFontOption.SYSTEM,
    val globalClockColorOption: ClockColorOption = ClockColorOption.THEME,
    val globalClockAccentColorOption: ClockColorOption = ClockColorOption.ACCENT_PRIMARY,
    val globalUse24HourTime: Boolean = false,
    val globalClockShowMeridiem: Boolean = false,
    val globalClockDateStyle: ClockDateStyle = ClockDateStyle.FULL,
    /** The calendar events strip's own font/color — part of the same Clock+Calendar design block/override, not a separate one (see chat history). */
    val globalCalendarFontOption: ClockFontOption = ClockFontOption.LAUNCHER_DEFAULT,
    val globalCalendarColorOption: ClockColorOption = ClockColorOption.THEME,
    val globalCalendarFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    /** Part of the same Clock+Calendar design bundle as the fields above — see chat history: moved in from being global-only. */
    val globalClockAlignment: ClockAlignment = ClockAlignment.LEFT,
    val globalCalendarAlignment: ClockAlignment = ClockAlignment.LEFT,
    val globalClockZoneHeightDp: Float? = null,
    val globalAppRowPosition: AppRowPosition = AppRowPosition.LEFT,
    val globalAppRowPresentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    val globalListContentMode: ListContentMode = ListContentMode.FAVORITES,
    val globalAppsToShowCount: Int = 5,
    val globalShowAllDayEvents: Boolean = true,
    /** `null` means nothing has been explicitly chosen yet — see [com.facetlauncher.app.data.model.LauncherSettings.selectedCalendarIds]. */
    val globalSelectedCalendarIds: Set<String>? = null,
) {
    /** The Clock card's single inherit/override switch — governs template, font, color, 24h, and meridiem. */
    val isOverridingClock: Boolean get() = facet?.overrideClock ?: false
    val clockTemplateId: ClockTemplateId get() = if (isOverridingClock) facet?.clockTemplateId ?: globalClockTemplateId else globalClockTemplateId
    val clockFontOption: ClockFontOption get() = if (isOverridingClock) facet?.clockFontOption ?: globalClockFontOption else globalClockFontOption
    val clockColorOption: ClockColorOption get() = if (isOverridingClock) facet?.clockColorOption ?: globalClockColorOption else globalClockColorOption
    val clockAccentColorOption: ClockColorOption get() = if (isOverridingClock) facet?.clockAccentColorOption ?: globalClockAccentColorOption else globalClockAccentColorOption
    val effectiveUse24HourTime: Boolean get() = if (isOverridingClock) facet?.use24HourTime ?: globalUse24HourTime else globalUse24HourTime
    val clockShowMeridiem: Boolean get() = if (isOverridingClock) facet?.clockShowMeridiem ?: globalClockShowMeridiem else globalClockShowMeridiem
    val clockDateStyle: ClockDateStyle get() = if (isOverridingClock) facet?.clockDateStyle ?: globalClockDateStyle else globalClockDateStyle
    val calendarFontOption: ClockFontOption get() = if (isOverridingClock) facet?.calendarFontOption ?: globalCalendarFontOption else globalCalendarFontOption
    val calendarColorOption: ClockColorOption get() = if (isOverridingClock) facet?.calendarColorOption ?: globalCalendarColorOption else globalCalendarColorOption
    val calendarFontWeight: FontWeightOption get() = if (isOverridingClock) facet?.calendarFontWeight ?: globalCalendarFontWeight else globalCalendarFontWeight
    val clockAlignment: ClockAlignment get() = if (isOverridingClock) facet?.clockAlignment ?: globalClockAlignment else globalClockAlignment
    val calendarAlignment: ClockAlignment get() = if (isOverridingClock) facet?.calendarAlignment ?: globalCalendarAlignment else globalCalendarAlignment
    val clockZoneHeightDp: Float? get() = if (isOverridingClock) facet?.clockZoneHeightDp ?: globalClockZoneHeightDp else globalClockZoneHeightDp

    /** The Apps card's single inherit/override switch — governs position, presentation, list content mode, apps-to-show, and favorites together. */
    val isOverridingApps: Boolean get() = facet?.overrideApps ?: false
    val appRowPosition: AppRowPosition get() = if (isOverridingApps) facet?.appRowPosition ?: globalAppRowPosition else globalAppRowPosition
    val appRowPresentation: AppRowPresentation get() = if (isOverridingApps) facet?.appRowPresentation ?: globalAppRowPresentation else globalAppRowPresentation
    val listContentMode: ListContentMode get() = if (isOverridingApps) facet?.listContentMode ?: globalListContentMode else globalListContentMode
    val appsToShowCount: Int get() = if (isOverridingApps) facet?.appsToShowCount ?: globalAppsToShowCount else globalAppsToShowCount
    val effectiveFavorites: List<AppInfo> get() = if (facet?.overridingFavorites == true) favorites else defaultFavorites
    val favoritesLabel: String get() = "${effectiveFavorites.size} of ${FavoriteAppRepository.MAX_FAVORITES}"

    /** The Dock card's single inherit/override switch — governs the dock's app list and its Icons/Text display style together. */
    val isOverridingDock: Boolean get() = facet?.overrideDock ?: false
    val dockDisplayMode: DockDisplayMode get() = if (isOverridingDock) facet?.dockDisplayMode ?: globalDockDisplayMode else globalDockDisplayMode
    val effectiveDockApps: List<AppInfo> get() = if (isOverridingDock) dockApps else defaultDockApps
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
            favoriteAppRepository.observeFavoritesForFacet(facetId),
            defaultFavoriteAppRepository.observeDefaultFavorites(),
            ::Pair,
        ),
        combine(
            facetDockAppRepository.observeDockAppsForFacet(facetId),
            dockAppRepository.observeDockApps(),
            ::Pair,
        ),
    ) { facets, settings, favs, docks ->
        val (favorites, defaultFavorites) = favs
        val (dockApps, defaultDockApps) = docks
        FacetSettingsUiState(
            facet = facets.find { it.id == facetId },
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
            globalCalendarFontOption = settings.calendarFontOption,
            globalCalendarColorOption = settings.calendarColorOption,
            globalCalendarFontWeight = settings.calendarFontWeight,
            globalClockAlignment = settings.clockAlignment,
            globalCalendarAlignment = settings.calendarAlignment,
            globalClockZoneHeightDp = settings.clockZoneHeightDp,
            globalAppRowPosition = settings.appRowPosition,
            globalAppRowPresentation = settings.appRowPresentation,
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

    /** Switching to Override seeds the facet's stored value with the current effective one; switching back to Inherit clears it. */
    fun setOverridingClock(overriding: Boolean) {
        val facet = uiState.value.facet ?: return
        val state = uiState.value
        viewModelScope.launch {
            facetRepository.updateOverridingClock(
                facet = facet,
                overriding = overriding,
                templateId = state.clockTemplateId,
                fontOption = state.clockFontOption,
                colorOption = state.clockColorOption,
                use24HourTime = state.effectiveUse24HourTime,
                showMeridiem = state.clockShowMeridiem,
                calendarFontOption = state.calendarFontOption,
                calendarColorOption = state.calendarColorOption,
                calendarFontWeight = state.calendarFontWeight,
                clockAlignment = state.clockAlignment,
                calendarAlignment = state.calendarAlignment,
                clockZoneHeightDp = state.clockZoneHeightDp,
                accentColorOption = state.clockAccentColorOption,
                dateStyle = state.clockDateStyle,
            )
        }
    }

    /**
     * The Apps card's single Inherit/Override switch — governs position, presentation, list
     * content mode, apps-to-show, and favorites as one unit. Switching to Override seeds all of
     * them with current effective values (including copying the default favorites list if the
     * facet's own list is empty) so the card doesn't suddenly go empty.
     */
    fun setOverridingApps(overriding: Boolean) {
        val facet = uiState.value.facet ?: return
        val state = uiState.value
        viewModelScope.launch {
            if (overriding && state.favorites.isEmpty()) {
                favoriteAppRepository.replaceFavorites(facetId, state.defaultFavorites)
            }
            facetRepository.updateOverridingApps(
                facet = facet,
                overriding = overriding,
                position = state.appRowPosition,
                presentation = state.appRowPresentation,
                mode = state.listContentMode,
                count = state.appsToShowCount,
                overridingFavorites = overriding,
            )
        }
    }

    /**
     * The Dock card's single Inherit/Override switch — governs the dock's app list and its
     * Icons/Text display style as one unit. Switching to Override seeds the display mode from the
     * current effective value and copies the default dock list in when the facet's own is empty,
     * so the card doesn't suddenly go blank — mirrors [setOverridingApps].
     */
    fun setOverridingDock(overriding: Boolean) {
        val facet = uiState.value.facet ?: return
        val state = uiState.value
        viewModelScope.launch {
            if (overriding && state.dockApps.isEmpty()) {
                facetDockAppRepository.replaceDockApps(facetId, state.defaultDockApps)
            }
            facetRepository.updateOverridingDock(facet, overriding, state.dockDisplayMode)
        }
    }
}
