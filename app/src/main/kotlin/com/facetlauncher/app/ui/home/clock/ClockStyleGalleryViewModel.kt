package com.facetlauncher.app.ui.home.clock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.ProfileRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.ProfileEntity
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NO_ACTIVE_PROFILE_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ClockStyleGalleryUiState(
    val templateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    val fontOption: ClockFontOption = ClockFontOption.SYSTEM,
    val colorOption: ClockColorOption = ClockColorOption.THEME,
    /** Only meaningful for templates where [com.facetlauncher.app.data.model.usesAccentColor] is true. */
    val accentColorOption: ClockColorOption = ClockColorOption.ACCENT_PRIMARY,
    val use24HourTime: Boolean = false,
    val showMeridiem: Boolean = false,
    val dateStyle: ClockDateStyle = ClockDateStyle.FULL,
    /** The calendar events strip's own font/color — part of this same Clock+Calendar design block/override, not a separate one (see chat history). */
    val calendarFontOption: ClockFontOption = ClockFontOption.LAUNCHER_DEFAULT,
    val calendarColorOption: ClockColorOption = ClockColorOption.THEME,
    val calendarFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    /** Global only — resolves [ClockFontOption.LAUNCHER_DEFAULT]'s preview here regardless of whether this instance is profile-scoped. */
    val launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
    /** Home clock's horizontal placement — part of the same Clock+Calendar design bundle as [templateId]/etc, so it resolves and persists the same way (this profile's own value when scoped, the global default otherwise). */
    val clockAlignment: ClockAlignment = ClockAlignment.LEFT,
    /** Home calendar strip's horizontal placement — independent of [clockAlignment], same resolution shape. */
    val calendarAlignment: ClockAlignment = ClockAlignment.LEFT,
)

/** Backs the global or profile-scoped clock style gallery. */
@HiltViewModel
class ClockStyleGalleryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val profileId: Long? = savedStateHandle.get<Long>("profileId")?.takeIf { it != NO_ACTIVE_PROFILE_ID }

    val uiState: StateFlow<ClockStyleGalleryUiState> = combine(
        settingsRepository.settings,
        profileRepository.observeProfiles(),
    ) { settings, profiles ->
        val profile = profiles.find { it.id == profileId }
        if (profile != null) {
            ClockStyleGalleryUiState(
                templateId = profile.clockTemplateId,
                fontOption = profile.clockFontOption,
                colorOption = profile.clockColorOption,
                accentColorOption = profile.clockAccentColorOption,
                use24HourTime = profile.use24HourTime,
                showMeridiem = profile.clockShowMeridiem,
                dateStyle = profile.clockDateStyle,
                calendarFontOption = profile.calendarFontOption,
                calendarColorOption = profile.calendarColorOption,
                calendarFontWeight = profile.calendarFontWeight,
                launcherFontOption = settings.launcherFontOption,
                clockAlignment = profile.clockAlignment,
                calendarAlignment = profile.calendarAlignment,
            )
        } else {
            ClockStyleGalleryUiState(
                templateId = settings.clockTemplateId,
                fontOption = settings.clockFontOption,
                colorOption = settings.clockColorOption,
                accentColorOption = settings.clockAccentColorOption,
                use24HourTime = settings.use24HourTime,
                showMeridiem = settings.clockShowMeridiem,
                dateStyle = settings.clockDateStyle,
                calendarFontOption = settings.calendarFontOption,
                calendarColorOption = settings.calendarColorOption,
                calendarFontWeight = settings.calendarFontWeight,
                launcherFontOption = settings.launcherFontOption,
                clockAlignment = settings.clockAlignment,
                calendarAlignment = settings.calendarAlignment,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClockStyleGalleryUiState())

    fun setClockTemplateId(id: ClockTemplateId) {
        viewModelScope.launch {
            profileId?.let { pid ->
                profileRepository.getById(pid)?.let { profileRepository.setClockTemplateId(it, id) }
            } ?: settingsRepository.setClockTemplateId(id)
        }
    }

    fun setClockFontOption(option: ClockFontOption) {
        viewModelScope.launch {
            profileId?.let { pid ->
                profileRepository.getById(pid)?.let { profileRepository.setClockFontOption(it, option) }
            } ?: settingsRepository.setClockFontOption(option)
        }
    }

    fun setClockColorOption(option: ClockColorOption) {
        viewModelScope.launch {
            profileId?.let { pid ->
                profileRepository.getById(pid)?.let { profileRepository.setClockColorOption(it, option) }
            } ?: settingsRepository.setClockColorOption(option)
        }
    }

    fun setClockAccentColorOption(option: ClockColorOption) {
        viewModelScope.launch {
            profileId?.let { pid ->
                profileRepository.getById(pid)?.let { profileRepository.setClockAccentColorOption(it, option) }
            } ?: settingsRepository.setClockAccentColorOption(option)
        }
    }

    fun setUse24HourTime(enabled: Boolean) {
        viewModelScope.launch {
            profileId?.let { pid ->
                profileRepository.getById(pid)?.let { profileRepository.setUse24HourTime(it, enabled) }
            } ?: settingsRepository.setUse24HourTime(enabled)
        }
    }

    fun setClockShowMeridiem(enabled: Boolean) {
        viewModelScope.launch {
            profileId?.let { pid ->
                profileRepository.getById(pid)?.let { profileRepository.setClockShowMeridiem(it, enabled) }
            } ?: settingsRepository.setClockShowMeridiem(enabled)
        }
    }

    fun setClockDateStyle(dateStyle: ClockDateStyle) {
        viewModelScope.launch {
            profileId?.let { pid ->
                profileRepository.getById(pid)?.let { profileRepository.setClockDateStyle(it, dateStyle) }
            } ?: settingsRepository.setClockDateStyle(dateStyle)
        }
    }

    fun setCalendarFontOption(option: ClockFontOption) {
        viewModelScope.launch {
            profileId?.let { pid ->
                profileRepository.getById(pid)?.let { profileRepository.setCalendarFontOption(it, option) }
            } ?: settingsRepository.setCalendarFontOption(option)
        }
    }

    fun setCalendarColorOption(option: ClockColorOption) {
        viewModelScope.launch {
            profileId?.let { pid ->
                profileRepository.getById(pid)?.let { profileRepository.setCalendarColorOption(it, option) }
            } ?: settingsRepository.setCalendarColorOption(option)
        }
    }

    fun setCalendarFontWeight(weight: FontWeightOption) {
        viewModelScope.launch {
            profileId?.let { pid ->
                profileRepository.getById(pid)?.let { profileRepository.setCalendarFontWeight(it, weight) }
            } ?: settingsRepository.setCalendarFontWeight(weight)
        }
    }

    fun setClockAlignment(alignment: ClockAlignment) {
        viewModelScope.launch {
            profileId?.let { pid ->
                profileRepository.getById(pid)?.let { profileRepository.setClockAlignment(it, alignment) }
            } ?: settingsRepository.setClockAlignment(alignment)
        }
    }

    /** Independent of [setClockAlignment]. */
    fun setCalendarAlignment(alignment: ClockAlignment) {
        viewModelScope.launch {
            profileId?.let { pid ->
                profileRepository.getById(pid)?.let { profileRepository.setCalendarAlignment(it, alignment) }
            } ?: settingsRepository.setCalendarAlignment(alignment)
        }
    }

    /**
     * "Reset clock widget position" — restores the whole clock+calendar widget to its original,
     * untouched layout: the zone height back to `null` (fixed-top, bottom-anchored app list),
     * BOTH alignments back to `LEFT`, and scale back to `0.8f`.
     * Resets this profile's own values when scoped, the global defaults otherwise — same branching
     * as every setter above.
     */
    fun resetClockPosition() {
        viewModelScope.launch {
            profileId?.let { pid ->
                profileRepository.getById(pid)?.let { profileRepository.resetClockPosition(it) }
            } ?: run {
                settingsRepository.resetClockZoneHeight()
                settingsRepository.resetClockScale()
                settingsRepository.setClockAlignment(ClockAlignment.LEFT)
                settingsRepository.setCalendarAlignment(ClockAlignment.LEFT)
            }
        }
    }
}
