package com.lumenlauncher.app.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.local.resolveOverride
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.ClockAlignment
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockDateStyle
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.domain.ObserveProfilePreviewsUseCase
import com.lumenlauncher.app.domain.ProfilePreviewData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileCarouselUiState(
    val profiles: List<ProfileEntity> = emptyList(),
    val activeProfileId: Long = 0L,
    val previewsByProfileId: Map<Long, ProfilePreviewData> = emptyMap(),
    val globalSettings: LauncherSettings = LauncherSettings(),
    // Dock is shared across all profiles (not per-profile — see DockAppRepository), so every
    // preview card renders the same dockApps/dockDisplayMode rather than one entry per profile.
    val dockApps: List<AppInfo> = emptyList(),
    val dockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
) {
    val canAddProfile: Boolean get() = profiles.size < ProfileRepository.MAX_PROFILES
    val canDeleteProfile: Boolean get() = profiles.size > ProfileRepository.MIN_PROFILES

    private fun profile(id: Long) = profiles.find { it.id == id }

    fun clockTemplateId(profileId: Long): ClockTemplateId =
        profile(profileId)?.let { if (it.overrideClock) it.clockTemplateId else globalSettings.clockTemplateId } ?: globalSettings.clockTemplateId

    fun clockFontOption(profileId: Long): ClockFontOption =
        profile(profileId)?.let { if (it.overrideClock) it.clockFontOption else globalSettings.clockFontOption } ?: globalSettings.clockFontOption

    fun clockColorOption(profileId: Long): ClockColorOption =
        profile(profileId)?.let { if (it.overrideClock) it.clockColorOption else globalSettings.clockColorOption } ?: globalSettings.clockColorOption

    /** See [com.lumenlauncher.app.data.model.LauncherSettings.clockAccentColorOption]. */
    fun clockAccentColorOption(profileId: Long): ClockColorOption =
        profile(profileId)?.let { if (it.overrideClock) it.clockAccentColorOption else globalSettings.clockAccentColorOption } ?: globalSettings.clockAccentColorOption

    fun effectiveUse24HourTime(profileId: Long): Boolean =
        profile(profileId)?.let { if (it.overrideClock) it.use24HourTime else globalSettings.use24HourTime } ?: globalSettings.use24HourTime

    fun clockShowMeridiem(profileId: Long): Boolean =
        profile(profileId)?.let { if (it.overrideClock) it.clockShowMeridiem else globalSettings.clockShowMeridiem } ?: globalSettings.clockShowMeridiem

    /** See [com.lumenlauncher.app.data.model.LauncherSettings.clockDateStyle]. */
    fun clockDateStyle(profileId: Long): ClockDateStyle =
        profile(profileId)?.let { if (it.overrideClock) it.clockDateStyle else globalSettings.clockDateStyle } ?: globalSettings.clockDateStyle

    fun clockAlignment(profileId: Long): ClockAlignment =
        profile(profileId).resolveOverride({ it.overrideClock }, { it.clockAlignment }, globalSettings.clockAlignment)

    /** Independent of [clockAlignment] — see [com.lumenlauncher.app.data.model.LauncherSettings.calendarAlignment]. */
    fun calendarAlignment(profileId: Long): ClockAlignment =
        profile(profileId).resolveOverride({ it.overrideClock }, { it.calendarAlignment }, globalSettings.calendarAlignment)

    fun clockZoneHeightDp(profileId: Long): Float? =
        profile(profileId).resolveOverride({ it.overrideClock }, { it.clockZoneHeightDp }, globalSettings.clockZoneHeightDp)

    fun clockScale(profileId: Long): Float =
        profile(profileId).resolveOverride({ it.overrideClock }, { it.clockScale }, globalSettings.clockScale)

    /** Mirrors [com.lumenlauncher.app.ui.home.HomeUiState.activeCalendarFontOption]'s own gating exactly — same `overrideCalendar` flag. */
    fun calendarFontOption(profileId: Long): ClockFontOption =
        profile(profileId)?.let { if (it.overrideCalendar) it.calendarFontOption else globalSettings.calendarFontOption } ?: globalSettings.calendarFontOption

    fun calendarColorOption(profileId: Long): ClockColorOption =
        profile(profileId)?.let { if (it.overrideCalendar) it.calendarColorOption else globalSettings.calendarColorOption } ?: globalSettings.calendarColorOption

    fun calendarFontWeight(profileId: Long): FontWeightOption =
        profile(profileId)?.let { if (it.overrideCalendar) it.calendarFontWeight else globalSettings.calendarFontWeight } ?: globalSettings.calendarFontWeight

    /** Mirrors [com.lumenlauncher.app.ui.home.HomeUiState.activeAppRowPosition]'s own gating exactly — same `overrideApps` flag. */
    fun appRowPosition(profileId: Long): AppRowPosition =
        profile(profileId)?.let { if (it.overrideApps) it.appRowPosition else globalSettings.appRowPosition } ?: globalSettings.appRowPosition

    fun appRowPresentation(profileId: Long): AppRowPresentation =
        profile(profileId)?.let { if (it.overrideApps) it.appRowPresentation else globalSettings.appRowPresentation } ?: globalSettings.appRowPresentation

    fun listContentMode(profileId: Long): ListContentMode =
        profile(profileId)?.let { if (it.overrideApps) it.listContentMode else globalSettings.listContentMode } ?: globalSettings.listContentMode
}

@HiltViewModel
class ProfileCarouselViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val dockAppRepository: DockAppRepository,
    observeProfilePreviews: ObserveProfilePreviewsUseCase,
) : ViewModel() {

    val uiState: StateFlow<ProfileCarouselUiState> = combine(
        profileRepository.observeProfiles(),
        settingsRepository.settings,
        observeProfilePreviews(),
        dockAppRepository.observeDockApps(),
    ) { profiles, settings, previewsByProfileId, dockApps ->
        ProfileCarouselUiState(
            profiles = profiles,
            activeProfileId = settings.activeProfileId,
            previewsByProfileId = previewsByProfileId,
            globalSettings = settings,
            dockApps = dockApps,
            dockDisplayMode = settings.dockDisplayMode,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileCarouselUiState())

    /** Applies the given profile as active — the carousel's browse position never persists on its own. */
    fun selectProfile(profileId: Long) {
        viewModelScope.launch { settingsRepository.setActiveProfileId(profileId) }
    }

    fun addProfile() {
        if (!uiState.value.canAddProfile) return
        viewModelScope.launch { profileRepository.addProfile() }
    }

    fun deleteProfile(profile: ProfileEntity) {
        if (!uiState.value.canDeleteProfile) return
        viewModelScope.launch {
            profileRepository.deleteProfile(profile)
            if (uiState.value.activeProfileId == profile.id) {
                val remaining = uiState.value.profiles.firstOrNull { it.id != profile.id }
                remaining?.let { settingsRepository.setActiveProfileId(it.id) }
            }
        }
    }

    fun renameProfile(profile: ProfileEntity, newName: String) {
        viewModelScope.launch { profileRepository.renameProfile(profile, newName) }
    }

    fun reorderProfiles(orderedProfiles: List<ProfileEntity>) {
        viewModelScope.launch { profileRepository.reorderProfiles(orderedProfiles) }
    }
}
