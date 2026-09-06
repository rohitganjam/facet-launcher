package com.lumenlauncher.app.ui.home.clock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.data.model.LauncherFontOption
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.data.model.NO_ACTIVE_PROFILE_ID
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
    val use24HourTime: Boolean = false,
    val showMeridiem: Boolean = false,
    /** The calendar events strip's own font/color — part of this same Clock+Calendar design block/override, not a separate one (see chat history). */
    val calendarFontOption: ClockFontOption = ClockFontOption.LAUNCHER_DEFAULT,
    val calendarColorOption: ClockColorOption = ClockColorOption.THEME,
    val calendarFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    /** Global only — resolves [ClockFontOption.LAUNCHER_DEFAULT]'s preview here regardless of whether this instance is profile-scoped. */
    val launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
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
                use24HourTime = profile.use24HourTime,
                showMeridiem = profile.clockShowMeridiem,
                calendarFontOption = profile.calendarFontOption,
                calendarColorOption = profile.calendarColorOption,
                calendarFontWeight = profile.calendarFontWeight,
                launcherFontOption = settings.launcherFontOption,
            )
        } else {
            ClockStyleGalleryUiState(
                templateId = settings.clockTemplateId,
                fontOption = settings.clockFontOption,
                colorOption = settings.clockColorOption,
                use24HourTime = settings.use24HourTime,
                showMeridiem = settings.clockShowMeridiem,
                calendarFontOption = settings.calendarFontOption,
                calendarColorOption = settings.calendarColorOption,
                calendarFontWeight = settings.calendarFontWeight,
                launcherFontOption = settings.launcherFontOption,
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
}
