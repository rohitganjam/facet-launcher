package com.facetlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.domain.resolveFacetStyle
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.CalendarInfo
import com.facetlauncher.app.data.model.ClockCalendarConfig
import com.facetlauncher.app.data.model.selectsCalendar
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.domain.AssignCalendarColorsUseCase
import com.facetlauncher.app.domain.UpdateClockConfigUseCase
import com.facetlauncher.app.ui.theme.AccentSwatch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalendarSettingsUiState(
    val facet: Facet? = null,
    /** Launcher-wide settings — a facet without a clock page of its own reads its defaults from here. */
    val settings: LauncherSettings = LauncherSettings(),
    val isCalendarAccessGranted: Boolean = false,
    val calendars: List<CalendarInfo> = emptyList(),
    /** Calendar id -> `AccentSwatch` enum name — see [com.facetlauncher.app.data.model.LauncherSettings.calendarColors]. Always global, never facet-scoped (a bar color isn't a "which calendars"/"show all-day" kind of decision). */
    val calendarColors: Map<String, String> = emptyMap(),
) {
    private val calendar: ClockCalendarConfig by lazy(LazyThreadSafetyMode.NONE) { resolveFacetStyle(facet, settings).clock.calendar }
    val effectiveShowAllDayEvents: Boolean get() = calendar.showAllDay
    val calendarDisplay: CalendarDisplayOptions get() = CalendarDisplayOptions(calendar.showIndicator, calendar.itemsToShow)
    /** `null` means every calendar is selected, calendars added later included. */
    val effectiveSelectedCalendarIds: Set<String>? get() = calendar.calendarIds
    /** The calendar's colour swatch, or `null` when the indicator is off (the picker then drops its colour dots too). */
    fun calendarSwatch(calendarId: String): AccentSwatch? =
        if (calendarDisplay.showIndicator) calendarColors[calendarId]?.let { runCatching { AccentSwatch.valueOf(it) }.getOrNull() } else null
    fun isCalendarSelected(calendarId: String): Boolean = effectiveSelectedCalendarIds.selectsCalendar(calendarId)
}

/** The calendar under one facet's clock: which calendars, all-day events, the indicator and how many events show. Call [bind] with the facet first. */
@HiltViewModel
class CalendarSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val calendarPermissionRepository: CalendarPermissionRepository,
    private val calendarRepository: CalendarRepository,
    private val assignCalendarColorsUseCase: AssignCalendarColorsUseCase,
    private val updateClockConfig: UpdateClockConfigUseCase,
) : ViewModel() {

    private val facetId = MutableStateFlow(NO_ACTIVE_FACET_ID)

    private val calendarColorPalette = AccentSwatch.entries.map { it.name }

    private val isCalendarAccessGranted = MutableStateFlow(calendarPermissionRepository.isGranted())
    private val calendars = MutableStateFlow<List<CalendarInfo>>(emptyList())

    val uiState: StateFlow<CalendarSettingsUiState> = combine(
        settingsRepository.settings,
        facetRepository.observeFacets(),
        facetId,
        combine(isCalendarAccessGranted, calendars) { granted, calendarList -> granted to calendarList },
    ) { settings, facets, id, (granted, calendarList) ->
        CalendarSettingsUiState(
            facet = facets.find { it.id == id },
            settings = settings,
            isCalendarAccessGranted = granted,
            calendars = calendarList,
            calendarColors = settings.calendarColors,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarSettingsUiState())

    init {
        refreshCalendarAccess()
    }

    /** Which facet's clock these settings edit; the sheet calls it each time it opens. */
    fun bind(id: Long) {
        facetId.value = id
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
        val id = facetId.value
        if (id == NO_ACTIVE_FACET_ID) return
        viewModelScope.launch { updateClockConfig(id) { it.copy(calendar = edit(it.calendar)) } }
    }
}
