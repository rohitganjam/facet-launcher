package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.local.resolveOverride
import com.facetlauncher.app.data.local.resolveSentinel
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.IconShape
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.SystemBarIconStyle
import com.facetlauncher.app.data.model.ThemeMode
import com.facetlauncher.app.data.model.WallpaperAccentRole
import com.facetlauncher.app.data.selectedCalendarIds
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

/**
 * `global*` are the raw stored global values; the effective, screen-bound `dockDisplayMode`/
 * `appRowPosition`/`appRowPresentation`/`appListVerticalAlignment` resolve facet-or-global via
 * their own `LAUNCHER_DEFAULT` sentinel (see [com.facetlauncher.app.data.local.resolveSentinel]) —
 * independent of `overrideDock`/`overrideApps`. `facetFavorites`/`globalFavorites` and
 * `facetDockItems`/`globalDockItems` are the raw stored lists; [effectiveFavorites]/
 * [effectiveDockItems] resolve them the same `overrideApps`/`overrideDock`-gated way
 * `HomeAppsListSettingsViewModel`/`DockSettingsViewModel` do — the preview card shows this
 * scope's real, actual content, not a sample.
 */
data class AppearanceSettingsUiState(
    val settings: LauncherSettings = LauncherSettings(),
    val facet: FacetEntity? = null,
    val facetFavorites: List<PlacedItem> = emptyList(),
    val globalFavorites: List<PlacedItem> = emptyList(),
    val facetDockItems: List<PlacedItem> = emptyList(),
    val globalDockItems: List<PlacedItem> = emptyList(),
    /** Today's real events for the effective calendar selection, empty when calendar permission isn't granted — never a sample fallback (matches `ObserveHomeScreenStateUseCase`'s own gating). */
    val calendarEvents: List<CalendarEvent> = emptyList(),
) {
    val isFacetScoped: Boolean get() = facet != null
    val dockDisplayMode: DockDisplayMode get() = facet.resolveSentinel({ it.dockDisplayMode }, DockDisplayMode.LAUNCHER_DEFAULT, settings.dockDisplayMode)
    val appRowPosition: AppRowPosition get() = facet.resolveSentinel({ it.appRowPosition }, AppRowPosition.LAUNCHER_DEFAULT, settings.appRowPosition)
    val appRowPresentation: AppRowPresentation get() = facet.resolveSentinel({ it.appRowPresentation }, AppRowPresentation.LAUNCHER_DEFAULT, settings.appRowPresentation)
    val appListVerticalAlignment: AppListVerticalAlignment get() = facet.resolveSentinel({ it.appListVerticalAlignment }, AppListVerticalAlignment.LAUNCHER_DEFAULT, settings.appListVerticalAlignment)
    val appListLayout: AppListLayout get() = facet.resolveSentinel({ it.appListLayout }, AppListLayout.LAUNCHER_DEFAULT, settings.appListLayout)
    val appListColumnAlignment: AppListColumnAlignment get() = facet.resolveSentinel({ it.appListColumnAlignment }, AppListColumnAlignment.LAUNCHER_DEFAULT, settings.appListColumnAlignment)
    val appListGridColumns: AppListGridColumns get() = facet.resolveSentinel({ it.appListGridColumns }, AppListGridColumns.LAUNCHER_DEFAULT, settings.appListGridColumns)
    val appListGridDisplayMode: AppListGridDisplayMode get() = facet.resolveSentinel({ it.appListGridDisplayMode }, AppListGridDisplayMode.LAUNCHER_DEFAULT, settings.appListGridDisplayMode)
    /** Mirrors `HomeAppsListSettingsViewModel.HomeAppsListUiState.favorites`'s own resolution exactly — same `overrideApps` flag. */
    val effectiveFavorites: List<PlacedItem> get() = if (facet?.overrideApps == true) facetFavorites else globalFavorites
    /** Mirrors `DockSettingsViewModel.DockSettingsUiState.dockItems`'s own resolution — same `overrideDock` flag. */
    val effectiveDockItems: List<PlacedItem> get() = if (facet?.overrideDock == true) facetDockItems else globalDockItems

    /** [settings] with the clock block resolved for this scope: the facet's own when `overrideClock`, else global. */
    val clockSettings: LauncherSettings get() = settings.copy(
        clockTemplateId = facet.resolveOverride({ it.overrideClock }, { it.clockTemplateId }, settings.clockTemplateId),
        clockFontOption = facet.resolveOverride({ it.overrideClock }, { it.clockFontOption }, settings.clockFontOption),
        clockColorOption = facet.resolveOverride({ it.overrideClock }, { it.clockColorOption }, settings.clockColorOption),
        clockAccentColorOption = facet.resolveOverride({ it.overrideClock }, { it.clockAccentColorOption }, settings.clockAccentColorOption),
        use24HourTime = facet.resolveOverride({ it.overrideClock }, { it.use24HourTime }, settings.use24HourTime),
        clockShowMeridiem = facet.resolveOverride({ it.overrideClock }, { it.clockShowMeridiem }, settings.clockShowMeridiem),
        clockDateStyle = facet.resolveOverride({ it.overrideClock }, { it.clockDateStyle }, settings.clockDateStyle),
        clockAlignment = facet.resolveOverride({ it.overrideClock }, { it.clockAlignment }, settings.clockAlignment),
    )
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
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val dockAppRepository: DockAppRepository,
    private val facetDockAppRepository: FacetDockAppRepository,
    private val calendarRepository: CalendarRepository,
    private val calendarPermissionRepository: CalendarPermissionRepository,
    private val wallpaperRepository: WallpaperRepository,
) : ViewModel() {

    private val facetId: Long? = savedStateHandle.get<Long>("facetId")?.takeIf { it != NO_ACTIVE_FACET_ID }

    private val facetFavoritesFlow: Flow<List<PlacedItem>> = facetId?.let { favoriteAppRepository.observeFavoriteItems(it) } ?: flowOf(emptyList())
    private val globalFavoritesFlow: Flow<List<PlacedItem>> = defaultFavoriteAppRepository.observeDefaultItems()
    private val facetDockItemsFlow: Flow<List<PlacedItem>> = facetId?.let { facetDockAppRepository.observeDockItems(it) } ?: flowOf(emptyList())
    private val globalDockItemsFlow: Flow<List<PlacedItem>> = dockAppRepository.observeDockItems()

    val uiState: StateFlow<AppearanceSettingsUiState> = combine(
        settingsRepository.settings,
        facetId?.let { facetRepository.observeFacets() } ?: flowOf(emptyList()),
        combine(facetFavoritesFlow, globalFavoritesFlow, ::Pair),
        combine(facetDockItemsFlow, globalDockItemsFlow, ::Pair),
    ) { settings, facets, favorites, dockItems ->
        val facet = facets.find { it.id == facetId }
        val (facetFavorites, globalFavorites) = favorites
        val (facetDockItems, globalDockItems) = dockItems
        AppearanceSettingsUiState(
            settings = settings,
            facet = facet,
            facetFavorites = facetFavorites,
            globalFavorites = globalFavorites,
            facetDockItems = facetDockItems,
            globalDockItems = globalDockItems,
            calendarEvents = observeCalendarEvents(facet, settings),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppearanceSettingsUiState())

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
    private suspend fun observeCalendarEvents(facet: FacetEntity?, settings: LauncherSettings): List<CalendarEvent> {
        if (!calendarPermissionRepository.isGranted()) return emptyList()
        val overriding = facet?.overrideCalendar == true
        val includeAllDay = if (overriding) facet.showAllDayEvents else settings.showAllDayEvents
        val selectedCalendarIds = if (overriding) facet.selectedCalendarIds else settings.selectedCalendarIds
        return calendarRepository.getTodayEvents(selectedCalendarIds, includeAllDay)
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setSystemBarIconStyle(style: SystemBarIconStyle) {
        viewModelScope.launch { settingsRepository.setSystemBarIconStyle(style) }
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

    /**
     * Facet-or-global write for this screen's four "look" fields — no Inherit/Override switch to
     * flip first, unlike Clock/Calendar/Apps/Dock: picking `LAUNCHER_DEFAULT` from the dropdown
     * itself is what reverts a facet to inheriting (see [com.facetlauncher.app.data.local.resolveSentinel],
     * and [com.facetlauncher.app.data.model.ClockFontOption]'s own precedent for this sentinel
     * model). No-op at global scope, where `LAUNCHER_DEFAULT` is filtered out of the option list.
     */
    fun setDockDisplayMode(mode: DockDisplayMode) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setDockDisplayMode(it, mode) } }
                ?: settingsRepository.setDockDisplayMode(mode)
        }
    }

    fun setAppRowPosition(position: AppRowPosition) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppRowPosition(it, position) } }
                ?: settingsRepository.setAppRowPosition(position)
        }
    }

    fun setAppRowPresentation(presentation: AppRowPresentation) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppRowPresentation(it, presentation) } }
                ?: settingsRepository.setAppRowPresentation(presentation)
        }
    }

    fun setAppListVerticalAlignment(alignment: AppListVerticalAlignment) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppListVerticalAlignment(it, alignment) } }
                ?: settingsRepository.setAppListVerticalAlignment(alignment)
        }
    }

    fun setAppListLayout(layout: AppListLayout) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppListLayout(it, layout) } }
                ?: settingsRepository.setAppListLayout(layout)
        }
    }

    fun setAppListColumnAlignment(alignment: AppListColumnAlignment) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppListColumnAlignment(it, alignment) } }
                ?: settingsRepository.setAppListColumnAlignment(alignment)
        }
    }

    fun setAppListGridColumns(columns: AppListGridColumns) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppListGridColumns(it, columns) } }
                ?: settingsRepository.setAppListGridColumns(columns)
        }
    }

    fun setAppListGridDisplayMode(mode: AppListGridDisplayMode) {
        viewModelScope.launch {
            facetId?.let { pid -> facetRepository.getById(pid)?.let { facetRepository.setAppListGridDisplayMode(it, mode) } }
                ?: settingsRepository.setAppListGridDisplayMode(mode)
        }
    }
}
