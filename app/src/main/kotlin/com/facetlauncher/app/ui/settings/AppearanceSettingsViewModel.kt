package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.landscape
import com.facetlauncher.app.domain.ApplyFacetDefaultsUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import com.facetlauncher.app.domain.FacetDefaultsScope
import com.facetlauncher.app.domain.ResolvedFacetStyle
import com.facetlauncher.app.domain.ObservePlacedItemsUseCase
import com.facetlauncher.app.domain.effectiveClockConfig
import com.facetlauncher.app.domain.resolveFacetStyle
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListShrinkToFit
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.IconShape
import com.facetlauncher.app.data.model.LandscapeOverrides
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.SystemBarIconStyle
import com.facetlauncher.app.data.model.ThemeMode
import com.facetlauncher.app.data.model.WallpaperAccentRole
import com.facetlauncher.app.ui.theme.AccentSwatch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.data.model.PlacementList

/**
 * At facet scope the screen-bound values are the facet's own; at launcher scope they are the launcher-wide defaults
 * a new facet is seeded from. `facetFavorites`/`globalFavorites` and `facetDockItems`/`globalDockItems` are the raw
 * stored lists; [effectiveFavorites]/[effectiveDockItems] pick this scope's — the preview card shows this
 * scope's real, actual content, not a sample.
 */
data class AppearanceSettingsUiState(
    val settings: LauncherSettings = LauncherSettings(),
    val facet: Facet? = null,
    /** Every facet, for the default screen's "Copy to facets". */
    val facets: ImmutableList<Facet> = persistentListOf(),
    val facetFavorites: List<PlacedItem> = emptyList(),
    val globalFavorites: List<PlacedItem> = emptyList(),
    val facetDockItems: List<PlacedItem> = emptyList(),
    val globalDockItems: List<PlacedItem> = emptyList(),
    /** Today's real events for the effective calendar selection, empty when calendar permission isn't granted — never a sample fallback (matches `ObserveHomeScreenStateUseCase`'s own gating). */
    val calendarEvents: List<CalendarEvent> = emptyList(),
    /** Whether notification-listener access is really granted — the Notifications row says so instead of reading "on" for a switch that isn't actually working. */
    val notificationAccessGranted: Boolean = false,
    /** Whether `READ_CALENDAR` is really granted — the Calendars row says so instead of summarising a selection nothing can read. */
    val calendarAccessGranted: Boolean = false,
) {
    val style: ResolvedFacetStyle by lazy(LazyThreadSafetyMode.NONE) { resolveFacetStyle(facet, settings) }
    val calendarDisplay: CalendarDisplayOptions get() = style.calendarDisplay
    val isFacetScoped: Boolean get() = facet != null
    val dockDisplayMode: DockDisplayMode get() = style.dockDisplayMode
    val appRowPosition: AppRowPosition get() = style.appList.rowPosition
    val appRowPresentation: AppRowPresentation get() = style.appList.rowPresentation
    val appListVerticalAlignment: AppListVerticalAlignment get() = style.appList.verticalAlignment
    val appListLayout: AppListLayout get() = style.appList.layout
    val appListColumnAlignment: AppListColumnAlignment get() = style.appList.columnAlignment
    /** Launcher-wide: off keeps Home in portrait, and the landscape settings below are hidden. */
    val allowLandscape: Boolean get() = settings.allowLandscape
    /** What the Landscape rows show: this scope's own landscape values, `null` meaning "same as portrait". */
    val landscape: LandscapeOverrides get() = facet?.landscape ?: settings.landscape
    val appListGridColumns: AppListGridColumns get() = style.appList.gridColumns
    val appListGridDisplayMode: AppListGridDisplayMode get() = style.appList.gridDisplayMode
    val appListShrinkToFit: AppListShrinkToFit get() = style.appList.shrinkToFit
    /** This scope's own Favorites: the facet's, or the launcher-wide default's at launcher scope. */
    val effectiveFavorites: List<PlacedItem> get() = if (facet != null) facetFavorites else globalFavorites
    /** This scope's own dock: the facet's, or the launcher-wide default's at launcher scope. */
    val effectiveDockItems: List<PlacedItem> get() = if (facet != null) facetDockItems else globalDockItems

    /** The calendar row's summary: this facet's clock's own calendar choices. */
    val effectiveShowAllDayEvents: Boolean get() = style.clock.calendar.showAllDay

    /** The calendars this facet's clock shows; `null` is every calendar, calendars added later included. */
    val selectedCalendarIds: Set<String>? get() = style.clock.calendar.calendarIds
}

/**
 * Settings → Appearance (`3c`'s theme/accent/icons/font block, split into its own screen — see
 * chat history: the main Settings list was getting too long to scan), and (with a `facetId`) a
 * facet's own Appearance screen for its "look" fields — dock display style, app-list presentation,
 * position, and vertical alignment (moved in from Dock's/Home-Apps-List's own screens, see chat
 * history: those are look/placement, not content, so they live here now, per-field
 * `LAUNCHER_DEFAULT`-sentinel overridable like [com.facetlauncher.app.data.model.ClockFontOption]).
 * Every other field here (theme, accent, icons, launcher font, app label color, font size/weight)
 * stays global only — no per-facet override exists for those yet.
 */
@HiltViewModel
class AppearanceSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val observePlacedItems: ObservePlacedItemsUseCase,
    private val calendarRepository: CalendarRepository,
    private val calendarPermissionRepository: CalendarPermissionRepository,
    private val wallpaperRepository: WallpaperRepository,
    private val notificationAccessRepository: NotificationAccessRepository,
    private val applyFacetDefaults: ApplyFacetDefaultsUseCase,
) : ViewModel() {

    private val facetId: Long? = savedStateHandle.get<Long>("facetId")?.takeIf { it != NO_ACTIVE_FACET_ID }

    private val facetFavoritesFlow: Flow<List<PlacedItem>> = facetId?.let { observePlacedItems.items(PlacementTarget(PlacementList.FAVORITES, it)) } ?: flowOf(emptyList())
    private val globalFavoritesFlow: Flow<List<PlacedItem>> = observePlacedItems.items(PlacementTarget.GlobalFavorites)
    private val facetDockItemsFlow: Flow<List<PlacedItem>> = facetId?.let { observePlacedItems.items(PlacementTarget(PlacementList.DOCK, it)) } ?: flowOf(emptyList())
    private val globalDockItemsFlow: Flow<List<PlacedItem>> = observePlacedItems.items(PlacementTarget.GlobalDock)

    private val isNotificationAccessGranted = MutableStateFlow(notificationAccessRepository.isGranted())
    private val isCalendarAccessGranted = MutableStateFlow(calendarPermissionRepository.isGranted())

    val uiState: StateFlow<AppearanceSettingsUiState> = combine(
        settingsRepository.settings,
        facetRepository.observeFacets(),
        combine(facetFavoritesFlow, globalFavoritesFlow, ::Pair),
        combine(facetDockItemsFlow, globalDockItemsFlow, ::Pair),
        combine(isNotificationAccessGranted, isCalendarAccessGranted, ::Pair),
    ) { settings, facets, favorites, dockItems, (notificationAccessGranted, calendarAccessGranted) ->
        val facet = facets.find { it.id == facetId }
        val (facetFavorites, globalFavorites) = favorites
        val (facetDockItems, globalDockItems) = dockItems
        AppearanceSettingsUiState(
            settings = settings,
            facet = facet,
            facets = facets.toImmutableList(),
            facetFavorites = facetFavorites,
            globalFavorites = globalFavorites,
            facetDockItems = facetDockItems,
            globalDockItems = globalDockItems,
            calendarEvents = observeCalendarEvents(facet, settings),
            notificationAccessGranted = notificationAccessGranted,
            calendarAccessGranted = calendarAccessGranted,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppearanceSettingsUiState())

    /** Notification-listener access has no grant-change callback — the screen calls this on resume, so returning from the Notifications page picks up a grant made in system Settings. */
    fun refreshNotificationAccess() {
        isNotificationAccessGranted.value = notificationAccessRepository.isGranted()
    }

    /** `READ_CALENDAR` has no grant-change callback either — called on resume, so granting it from the Calendars page updates this screen. Re-emitting also re-reads today's events for the preview. */
    fun refreshCalendarAccess() {
        isCalendarAccessGranted.value = calendarPermissionRepository.isGranted()
    }

    val settings: StateFlow<LauncherSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LauncherSettings())

    private val _homeWallpaper = MutableStateFlow<HomeWallpaper>(HomeWallpaper.Unavailable)

    /** The live system wallpaper, painted behind the preview card so it reads as real Home,
     *  not a flat slab. Loaded once when the screen opens. */
    val homeWallpaper: StateFlow<HomeWallpaper> = _homeWallpaper

    init {
        viewModelScope.launch { _homeWallpaper.value = wallpaperRepository.currentHomeWallpaper() }
    }

    /**
     * Today's real events for [facet]'s (or the global default's) effective calendar selection —
     * mirrors `ObserveHomeScreenStateUseCase.observeCalendarEvents`'s own resolution shape exactly
     * (facet-or-global `showAllDayEvents`/`selectedCalendarIds`, gated by calendar permission).
     * Called from inside the `uiState` combine lambda rather than composed as its own Flow, since
     * it needs both [facet] and [settings] together and this screen has no resume-driven refresh
     * trigger to justify `ObserveHomeScreenStateUseCase`'s extra `flow {}` wrapping — a plain
     * `suspend` re-fetch on every `combine` emission is enough for a settings preview.
     */
    private suspend fun observeCalendarEvents(facet: Facet?, settings: LauncherSettings): List<CalendarEvent> {
        if (!calendarPermissionRepository.isGranted()) return emptyList()
        val calendar = effectiveClockConfig(facet, settings).calendar
        return calendarRepository.getUpcomingEvents(calendar.calendarIds, calendar.showAllDay)
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setSystemBarIconStyle(style: SystemBarIconStyle) {
        viewModelScope.launch { settingsRepository.setSystemBarIconStyle(style) }
    }

    fun setHideStatusBar(hide: Boolean) {
        viewModelScope.launch { settingsRepository.setHideStatusBar(hide) }
    }

    fun setAccentFromSystem(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAccentFromSystem(enabled) }
    }

    fun setCustomAccentSwatch(swatch: AccentSwatch) {
        viewModelScope.launch { settingsRepository.setCustomAccentSwatch(swatch.name) }
    }

    fun setWallpaperAccentRole(role: WallpaperAccentRole) {
        viewModelScope.launch { settingsRepository.setWallpaperAccentRole(role) }
    }

    fun setIconRenderMode(mode: IconRenderMode) {
        viewModelScope.launch { settingsRepository.setIconRenderMode(mode) }
    }

    fun setIconShape(shape: IconShape) {
        viewModelScope.launch { settingsRepository.setIconShape(shape) }
    }

    fun setIconScalePercent(percent: Int) {
        viewModelScope.launch { settingsRepository.setIconScalePercent(percent) }
    }

    fun setLauncherFontOption(option: LauncherFontOption) {
        viewModelScope.launch { settingsRepository.setLauncherFontOption(option) }
    }

    fun setFontScaleOption(option: FontScaleOption) {
        viewModelScope.launch { settingsRepository.setFontScaleOption(option) }
    }

    fun setAppLabelColorOption(option: ClockColorOption) {
        viewModelScope.launch { settingsRepository.setAppLabelColorOption(option) }
    }

    fun setHomeAppsFontWeight(weight: FontWeightOption) {
        viewModelScope.launch { settingsRepository.setHomeAppsFontWeight(weight) }
    }

    /** "Copy to facets": replaces the dock and apps look of each chosen facet with the launcher-wide one. */
    fun copyLookToFacets(facetIds: Set<Long>) {
        viewModelScope.launch { facetIds.forEach { applyFacetDefaults(it, FacetDefaultsScope.LOOK) } }
    }

    /** "Reset to default": replaces this facet's dock and apps look with the launcher-wide one. */
    fun resetLookToDefault() {
        val id = facetId ?: return
        viewModelScope.launch { applyFacetDefaults(id, FacetDefaultsScope.LOOK) }
    }

    /** Facet-or-global write: a facet's own value, or the launcher-wide default a new facet is seeded from. */
    fun setDockDisplayMode(mode: DockDisplayMode) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setDockDisplayMode(it.id, mode) } }
                ?: settingsRepository.setDockDisplayMode(mode)
        }
    }

    fun setAppRowPosition(position: AppRowPosition) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppRowPosition(it.id, position) } }
                ?: settingsRepository.setAppRowPosition(position)
        }
    }

    fun setAllowLandscape(allow: Boolean) {
        viewModelScope.launch { settingsRepository.setAllowLandscape(allow) }
    }

    fun setLandscapeAppRowPosition(position: AppRowPosition?) = updateLandscape { it.copy(appRowPosition = position) }

    fun setLandscapeAppListVerticalAlignment(alignment: AppListVerticalAlignment?) = updateLandscape { it.copy(appListVerticalAlignment = alignment) }

    fun setLandscapeAppListColumnAlignment(alignment: AppListColumnAlignment?) = updateLandscape { it.copy(appListColumnAlignment = alignment) }

    /** This scope's landscape values: the facet's own when facet-scoped, the launcher-wide ones otherwise. */
    private fun updateLandscape(transform: (LandscapeOverrides) -> LandscapeOverrides) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.updateLandscape(it.id, transform) } }
                ?: settingsRepository.updateLandscape(transform)
        }
    }

    fun setAppRowPresentation(presentation: AppRowPresentation) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppRowPresentation(it.id, presentation) } }
                ?: settingsRepository.setAppRowPresentation(presentation)
        }
    }

    fun setAppListVerticalAlignment(alignment: AppListVerticalAlignment) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppListVerticalAlignment(it.id, alignment) } }
                ?: settingsRepository.setAppListVerticalAlignment(alignment)
        }
    }

    fun setAppListLayout(layout: AppListLayout) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppListLayout(it.id, layout) } }
                ?: settingsRepository.setAppListLayout(layout)
        }
    }

    fun setAppListColumnAlignment(alignment: AppListColumnAlignment) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppListColumnAlignment(it.id, alignment) } }
                ?: settingsRepository.setAppListColumnAlignment(alignment)
        }
    }

    fun setAppListGridColumns(columns: AppListGridColumns) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppListGridColumns(it.id, columns) } }
                ?: settingsRepository.setAppListGridColumns(columns)
        }
    }

    fun setAppListShrinkToFit(shrinkToFit: AppListShrinkToFit) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppListShrinkToFit(it.id, shrinkToFit) } }
                ?: settingsRepository.setAppListShrinkToFit(shrinkToFit)
        }
    }

    fun setAppListGridDisplayMode(mode: AppListGridDisplayMode) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppListGridDisplayMode(it.id, mode) } }
                ?: settingsRepository.setAppListGridDisplayMode(mode)
        }
    }
}
