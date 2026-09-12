package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.CalendarInfo
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.selectedCalendarIds
import com.facetlauncher.app.domain.AssignCalendarColorsUseCase
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
    val facet: FacetEntity? = null,
    val globalShowAllDayEvents: Boolean = true,
    /** `null` means every calendar is implicitly selected — see [com.facetlauncher.app.data.model.LauncherSettings.selectedCalendarIds]. */
    val globalSelectedCalendarIds: Set<String>? = null,
    val isCalendarAccessGranted: Boolean = false,
    val calendars: List<CalendarInfo> = emptyList(),
    /** Calendar id -> `AccentSwatch` enum name — see [com.facetlauncher.app.data.model.LauncherSettings.calendarColors]. Always global, never facet-scoped (a bar color isn't a "which calendars"/"show all-day" kind of decision). */
    val calendarColors: Map<String, String> = emptyMap(),
) {
    val isFacetScoped: Boolean get() = facet != null
    val isOverriding: Boolean get() = facet?.overrideCalendar ?: false
    val effectiveShowAllDayEvents: Boolean get() = if (isOverriding) facet?.showAllDayEvents ?: globalShowAllDayEvents else globalShowAllDayEvents
    val effectiveSelectedCalendarIds: Set<String>? get() = if (isOverriding) facet?.selectedCalendarIds ?: globalSelectedCalendarIds else globalSelectedCalendarIds
    fun isCalendarSelected(calendarId: String): Boolean = effectiveSelectedCalendarIds?.let { calendarId in it } ?: true
}

/**
 * Calendar *selection* settings (`4l`'s "Calendars to display"/all-day-events — content decisions,
 * not design ones). Reached from the global Settings Clock card (no facet scope) or a facet's
 * Clock card (scoped, with its own inherit/override header per `3f`'s pattern) — deliberately
 * separate from the Clock+Calendar *design* settings (font/color/weight/template), which now live
 * on `ClockStyleGalleryScreen` under `overrideClock` instead, since the whole Home clock/calendar
 * block reads as one visual unit (see chat history). No `facetId` (or [NO_ACTIVE_FACET_ID])
 * means the global Settings entry point — the toggle then writes [SettingsRepository] directly
 * instead of a per-facet override.
 */
@HiltViewModel
class CalendarSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val calendarPermissionRepository: CalendarPermissionRepository,
    private val calendarRepository: CalendarRepository,
) : ViewModel() {

    private val facetId: Long = savedStateHandle.get<Long>("facetId") ?: NO_ACTIVE_FACET_ID

    private val assignCalendarColorsUseCase = AssignCalendarColorsUseCase()
    private val calendarColorPalette = AccentSwatch.entries.map { it.name }

    private val isCalendarAccessGranted = MutableStateFlow(calendarPermissionRepository.isGranted())
    private val calendars = MutableStateFlow<List<CalendarInfo>>(emptyList())

    val uiState: StateFlow<CalendarSettingsUiState> = combine(
        settingsRepository.settings,
        facetRepository.observeFacets(),
        isCalendarAccessGranted,
        calendars,
    ) { settings, facets, granted, calendarList ->
        CalendarSettingsUiState(
            facet = if (facetId == NO_ACTIVE_FACET_ID) null else facets.find { it.id == facetId },
            globalShowAllDayEvents = settings.showAllDayEvents,
            globalSelectedCalendarIds = settings.selectedCalendarIds,
            isCalendarAccessGranted = granted,
            calendars = calendarList,
            calendarColors = settings.calendarColors,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarSettingsUiState())

    init {
        refreshCalendarAccess()
    }

    /** `READ_CALENDAR` has no grant-change callback — call on resume, and right after the runtime request returns. */
    fun refreshCalendarAccess() {
        val granted = calendarPermissionRepository.isGranted()
        isCalendarAccessGranted.value = granted
        if (granted) {
            viewModelScope.launch {
                val calendarList = calendarRepository.getCalendars()
                calendars.value = calendarList

                // Every calendar gets a stable bar color the moment this list loads — not just
                // the ones currently selected — so a color is already there to preview before
                // picking, and toggling a calendar off and back on doesn't reshuffle anything.
                val existing = uiState.value.calendarColors
                val updated = assignCalendarColorsUseCase(calendarList.map { it.id }, existing, calendarColorPalette)
                if (updated != existing) settingsRepository.setCalendarColors(updated)
            }
        }
    }

    fun setShowAllDayEvents(enabled: Boolean) {
        val facet = uiState.value.facet
        viewModelScope.launch {
            if (facet != null) {
                facetRepository.setShowAllDayEvents(facet, enabled)
            } else {
                settingsRepository.setShowAllDayEvents(enabled)
            }
        }
    }

    /** Switching to Override seeds the facet's stored values with the current effective ones; switching back to Inherit clears it. */
    fun setOverriding(overriding: Boolean) {
        val facet = uiState.value.facet ?: return
        val state = uiState.value
        viewModelScope.launch {
            facetRepository.updateOverridingCalendar(
                facet = facet,
                overriding = overriding,
                showAllDayEvents = state.effectiveShowAllDayEvents,
                selectedCalendarIds = state.effectiveSelectedCalendarIds,
            )
        }
    }

    /** Toggling one calendar materializes an implicit "all selected" (`null`) into an explicit set first — writes the facet's own override when overriding, otherwise the global default (same routing [setShowAllDayEvents] already uses). */
    fun setCalendarSelected(calendarId: String, selected: Boolean) {
        val state = uiState.value
        val current = state.effectiveSelectedCalendarIds ?: state.calendars.map { it.id }.toSet()
        val updated = if (selected) current + calendarId else current - calendarId
        writeSelectedCalendarIds(state.facet, updated)
    }

    /**
     * The "Calendars to display" header checkbox — selects or deselects every currently-loaded
     * calendar in one tap, rather than requiring one tap per calendar (real pain with a lot of
     * calendars, see chat history).
     */
    fun setAllCalendarsSelected(selected: Boolean) {
        val state = uiState.value
        val ids = if (selected) state.calendars.map { it.id }.toSet() else emptySet()
        writeSelectedCalendarIds(state.facet, ids)
    }

    private fun writeSelectedCalendarIds(facet: FacetEntity?, ids: Set<String>) {
        viewModelScope.launch {
            if (facet != null && facet.overrideCalendar) {
                facetRepository.setSelectedCalendarIds(facet, ids)
            } else {
                settingsRepository.setSelectedCalendarIds(ids)
            }
        }
    }
}
