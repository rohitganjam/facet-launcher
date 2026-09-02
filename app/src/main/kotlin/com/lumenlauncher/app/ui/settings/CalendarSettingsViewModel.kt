package com.lumenlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.CalendarPermissionRepository
import com.lumenlauncher.app.data.CalendarRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.CalendarInfo
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.NO_ACTIVE_PROFILE_ID
import com.lumenlauncher.app.domain.AssignCalendarColorsUseCase
import com.lumenlauncher.app.ui.theme.AccentSwatch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalendarSettingsUiState(
    val profile: ProfileEntity? = null,
    val globalShowAllDayEvents: Boolean = true,
    val isCalendarAccessGranted: Boolean = false,
    val calendars: List<CalendarInfo> = emptyList(),
    /** `null` means every calendar is implicitly selected — see [com.lumenlauncher.app.data.model.LauncherSettings.selectedCalendarIds]. */
    val selectedCalendarIds: Set<String>? = null,
    /** Calendar id -> `AccentSwatch` enum name — see [com.lumenlauncher.app.data.model.LauncherSettings.calendarColors]. */
    val calendarColors: Map<String, String> = emptyMap(),
    /** The calendar events block's own font/color — global only (no per-profile override entity exists; a profile-scoped screen keeps these local/unpersisted for evaluation, see `CalendarSettingsScreen.kt`). */
    val globalCalendarFontOption: ClockFontOption = ClockFontOption.SYSTEM,
    val globalCalendarColorOption: ClockColorOption = ClockColorOption.INK,
) {
    val isProfileScoped: Boolean get() = profile != null
    val isOverriding: Boolean get() = profile?.overrideCalendar ?: false
    val effectiveShowAllDayEvents: Boolean get() = if (isOverriding) profile?.showAllDayEvents ?: globalShowAllDayEvents else globalShowAllDayEvents
    fun isCalendarSelected(calendarId: String): Boolean = selectedCalendarIds?.let { calendarId in it } ?: true
}

/**
 * Calendar settings (`4l`, plus the new inherit/override header when reached from a profile's
 * Clock card). No `profileId` (or [NO_ACTIVE_PROFILE_ID]) means the global Settings entry point —
 * the toggle then writes [SettingsRepository] directly instead of a per-profile override.
 */
@HiltViewModel
class CalendarSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val calendarPermissionRepository: CalendarPermissionRepository,
    private val calendarRepository: CalendarRepository,
) : ViewModel() {

    private val profileId: Long = savedStateHandle.get<Long>("profileId") ?: NO_ACTIVE_PROFILE_ID

    private val assignCalendarColorsUseCase = AssignCalendarColorsUseCase()
    private val calendarColorPalette = AccentSwatch.entries.map { it.name }

    private val isCalendarAccessGranted = MutableStateFlow(calendarPermissionRepository.isGranted())
    private val calendars = MutableStateFlow<List<CalendarInfo>>(emptyList())

    val uiState: StateFlow<CalendarSettingsUiState> = combine(
        settingsRepository.settings,
        profileRepository.observeProfiles(),
        isCalendarAccessGranted,
        calendars,
    ) { settings, profiles, granted, calendarList ->
        CalendarSettingsUiState(
            profile = if (profileId == NO_ACTIVE_PROFILE_ID) null else profiles.find { it.id == profileId },
            globalShowAllDayEvents = settings.showAllDayEvents,
            isCalendarAccessGranted = granted,
            calendars = calendarList,
            selectedCalendarIds = settings.selectedCalendarIds,
            calendarColors = settings.calendarColors,
            globalCalendarFontOption = settings.calendarFontOption,
            globalCalendarColorOption = settings.calendarColorOption,
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
        val profile = uiState.value.profile
        viewModelScope.launch {
            if (profile != null) {
                profileRepository.setShowAllDayEvents(profile, enabled)
            } else {
                settingsRepository.setShowAllDayEvents(enabled)
            }
        }
    }

    /** Switching to Override seeds the profile's stored value with the current effective one; switching back to Inherit clears it. */
    fun setOverriding(overriding: Boolean) {
        val profile = uiState.value.profile ?: return
        viewModelScope.launch {
            profileRepository.updateOverridingCalendar(
                profile = profile,
                overriding = overriding,
                showAllDayEvents = uiState.value.effectiveShowAllDayEvents,
            )
        }
    }

    /** Toggling one calendar materializes an implicit "all selected" (`null`) into an explicit set first. */
    fun setCalendarSelected(calendarId: String, selected: Boolean) {
        val state = uiState.value
        val current = state.selectedCalendarIds ?: state.calendars.map { it.id }.toSet()
        val updated = if (selected) current + calendarId else current - calendarId
        viewModelScope.launch { settingsRepository.setSelectedCalendarIds(updated) }
    }

    /**
     * The "Calendars to display" header checkbox — selects or deselects every currently-loaded
     * calendar in one tap, rather than requiring one tap per calendar (real pain with a lot of
     * calendars, see chat history).
     */
    fun setAllCalendarsSelected(selected: Boolean) {
        val ids = if (selected) uiState.value.calendars.map { it.id }.toSet() else emptySet()
        viewModelScope.launch { settingsRepository.setSelectedCalendarIds(ids) }
    }

    /** Always writes the global setting — there's no per-profile calendar font/color override entity. */
    fun setGlobalCalendarFontOption(option: ClockFontOption) {
        viewModelScope.launch { settingsRepository.setCalendarFontOption(option) }
    }

    fun setGlobalCalendarColorOption(option: ClockColorOption) {
        viewModelScope.launch { settingsRepository.setCalendarColorOption(option) }
    }
}
