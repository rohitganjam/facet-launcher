package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.domain.ResolvedFacetStyle
import com.facetlauncher.app.domain.resolveFacetStyle
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.CalendarInfo
import com.facetlauncher.app.data.model.ClockCalendarConfig
import com.facetlauncher.app.data.model.selectsCalendar
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.domain.AssignCalendarColorsUseCase
import com.facetlauncher.app.domain.UpdateClockConfigUseCase
import com.facetlauncher.app.ui.theme.AccentSwatch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalendarSettingsUiState(
    val facet: Facet? = null,
    /** Launcher-wide settings — the preview strip reads the font/colour fields from here. */
    val settings: LauncherSettings = LauncherSettings(),
    val homeWallpaper: HomeWallpaper = HomeWallpaper.Unavailable,
    val isCalendarAccessGranted: Boolean = false,
    val calendars: List<CalendarInfo> = emptyList(),
    /** Real upcoming events (midnight today to noon tomorrow) for the effective selection; empty when access is off or none are due. */
    val previewEvents: ImmutableList<CalendarEvent> = persistentListOf(),
    /** Calendar id -> `AccentSwatch` enum name — see [com.facetlauncher.app.data.model.LauncherSettings.calendarColors]. Always global, never facet-scoped (a bar color isn't a "which calendars"/"show all-day" kind of decision). */
    val calendarColors: Map<String, String> = emptyMap(),
) {
    private val style: ResolvedFacetStyle by lazy(LazyThreadSafetyMode.NONE) { resolveFacetStyle(facet, settings) }
    private val calendar: ClockCalendarConfig get() = style.clock.calendar
    val effectiveShowAllDayEvents: Boolean get() = calendar.showAllDay
    val calendarDisplay: CalendarDisplayOptions get() = CalendarDisplayOptions(calendar.showIndicator, calendar.itemsToShow)
    /** The clock-block look the preview strip renders with. */
    val previewUse24HourTime: Boolean get() = style.clock.use24HourTime
    val previewClockAlignment: ClockAlignment get() = style.clockPlacement.portrait.alignment
    /** `null` means every calendar is selected, calendars added later included. */
    val effectiveSelectedCalendarIds: Set<String>? get() = calendar.calendarIds
    /** The calendar's colour swatch, or `null` when the indicator is off (the picker then drops its colour dots too). */
    fun calendarSwatch(calendarId: String): AccentSwatch? =
        if (calendarDisplay.showIndicator) calendarColors[calendarId]?.let { runCatching { AccentSwatch.valueOf(it) }.getOrNull() } else null
    fun isCalendarSelected(calendarId: String): Boolean = effectiveSelectedCalendarIds.selectsCalendar(calendarId)
}

/** The calendar under one facet's clock: which calendars, all-day events, the indicator and how many events show. */
@HiltViewModel
class CalendarSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val calendarPermissionRepository: CalendarPermissionRepository,
    private val calendarRepository: CalendarRepository,
    private val wallpaperRepository: WallpaperRepository,
    private val assignCalendarColorsUseCase: AssignCalendarColorsUseCase,
    private val updateClockConfig: UpdateClockConfigUseCase,
) : ViewModel() {

    private val facetId: Long = savedStateHandle.get<Long>("facetId") ?: NO_ACTIVE_FACET_ID

    private val calendarColorPalette = AccentSwatch.entries.map { it.name }

    private val isCalendarAccessGranted = MutableStateFlow(calendarPermissionRepository.isGranted())
    private val calendars = MutableStateFlow<List<CalendarInfo>>(emptyList())
    private val homeWallpaper = MutableStateFlow<HomeWallpaper>(HomeWallpaper.Unavailable)

    private val previewEvents = MutableStateFlow<List<CalendarEvent>>(emptyList())

    private fun buildUiState(
        settings: LauncherSettings,
        facets: List<Facet>,
        granted: Boolean,
        calendarList: List<CalendarInfo> = emptyList(),
        wallpaper: HomeWallpaper = HomeWallpaper.Unavailable,
        events: List<CalendarEvent> = emptyList(),
    ) = CalendarSettingsUiState(
        facet = if (facetId == NO_ACTIVE_FACET_ID) null else facets.find { it.id == facetId },
        settings = settings,
        homeWallpaper = wallpaper,
        isCalendarAccessGranted = granted,
        calendars = calendarList,
        previewEvents = events.toImmutableList(),
        calendarColors = settings.calendarColors,
    )

    val uiState: StateFlow<CalendarSettingsUiState> = combine(
        settingsRepository.settings,
        facetRepository.observeFacets(),
        combine(isCalendarAccessGranted, calendars, previewEvents) { granted, calendarList, events -> Triple(granted, calendarList, events) },
        homeWallpaper,
    ) { settings, facets, (granted, calendarList, events), wallpaper ->
        buildUiState(settings, facets, granted, calendarList, wallpaper, events)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarSettingsUiState())

    init {
        refreshCalendarAccess()
        viewModelScope.launch { homeWallpaper.value = wallpaperRepository.currentHomeWallpaper() }
        // Reload the preview's events whenever access or the effective calendar selection changes.
        viewModelScope.launch {
            combine(settingsRepository.settings, facetRepository.observeFacets(), isCalendarAccessGranted) { settings, facets, granted ->
                buildUiState(settings, facets, granted)
            }.map { Triple(it.isCalendarAccessGranted, it.effectiveSelectedCalendarIds, it.effectiveShowAllDayEvents) }
                .distinctUntilChanged()
                .collectLatest { (granted, calendarIds, includeAllDay) ->
                    previewEvents.value = if (granted) calendarRepository.getUpcomingEvents(calendarIds, includeAllDay) else emptyList()
                }
        }
    }

    /** `READ_CALENDAR` has no grant-change callback — call on resume, and right after the runtime request returns. */
    fun refreshCalendarAccess() {
        val granted = calendarPermissionRepository.isGranted()
        isCalendarAccessGranted.value = granted
        if (granted) {
            viewModelScope.launch {
                val calendarList = calendarRepository.getSelectableCalendars()
                calendars.value = calendarList

                // Every calendar gets a stable bar color the moment this list loads — not just
                // the ones currently selected — so a color is already there to preview before
                // picking, and toggling a calendar off and back on doesn't reshuffle anything.
                val existing = uiState.value.calendarColors
                val updated = assignCalendarColorsUseCase(calendarList.map { it.id }, existing, calendarColorPalette)
                if (updated != existing) settingsRepository.setCalendarColors(updated)
            }
        } else {
            calendars.value = emptyList()
        }
    }

    fun setShowAllDayEvents(enabled: Boolean) = changeCalendar { it.copy(showAllDay = enabled) }

    fun setShowCalendarIndicator(enabled: Boolean) = changeCalendar { it.copy(showIndicator = enabled) }

    fun setCalendarItemsToShow(count: Int) = changeCalendar { it.copy(itemsToShow = count) }

    /** Toggling one calendar turns "all calendars" into an explicit set first, so it only changes this one. */
    fun setCalendarSelected(calendarId: String, selected: Boolean) {
        val state = uiState.value
        val current = state.calendars.filter { state.isCalendarSelected(it.id) }.map { it.id }.toSet()
        changeCalendar { it.copy(calendarIds = if (selected) current + calendarId else current - calendarId) }
    }

    /**
     * The "Calendars to display" header checkbox — selects or deselects every currently-loaded
     * calendar in one tap, rather than requiring one tap per calendar.
     */
    fun setAllCalendarsSelected(selected: Boolean) {
        val ids = if (selected) uiState.value.calendars.map { it.id }.toSet() else emptySet()
        changeCalendar { it.copy(calendarIds = ids) }
    }

    private fun changeCalendar(edit: (ClockCalendarConfig) -> ClockCalendarConfig) {
        if (facetId == NO_ACTIVE_FACET_ID) return
        viewModelScope.launch { updateClockConfig(facetId) { it.copy(calendar = edit(it.calendar)) } }
    }
}
