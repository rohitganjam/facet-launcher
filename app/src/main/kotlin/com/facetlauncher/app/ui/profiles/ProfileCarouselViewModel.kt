package com.facetlauncher.app.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.ProfileRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.ProfileEntity
import com.facetlauncher.app.data.local.resolveOverride
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
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.domain.ObserveProfilePreviewsUseCase
import com.facetlauncher.app.domain.ProfilePreviewData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
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
    // The live system wallpaper, rendered behind each preview card (Home's own window-level
    // wallpaper compositing can't reach a card). Loaded once when the carousel opens.
    val homeWallpaper: HomeWallpaper = HomeWallpaper.Unavailable,
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

    /** See [com.facetlauncher.app.data.model.LauncherSettings.clockAccentColorOption]. */
    fun clockAccentColorOption(profileId: Long): ClockColorOption =
        profile(profileId)?.let { if (it.overrideClock) it.clockAccentColorOption else globalSettings.clockAccentColorOption } ?: globalSettings.clockAccentColorOption

    fun effectiveUse24HourTime(profileId: Long): Boolean =
        profile(profileId)?.let { if (it.overrideClock) it.use24HourTime else globalSettings.use24HourTime } ?: globalSettings.use24HourTime

    fun clockShowMeridiem(profileId: Long): Boolean =
        profile(profileId)?.let { if (it.overrideClock) it.clockShowMeridiem else globalSettings.clockShowMeridiem } ?: globalSettings.clockShowMeridiem

    /** See [com.facetlauncher.app.data.model.LauncherSettings.clockDateStyle]. */
    fun clockDateStyle(profileId: Long): ClockDateStyle =
        profile(profileId)?.let { if (it.overrideClock) it.clockDateStyle else globalSettings.clockDateStyle } ?: globalSettings.clockDateStyle

    fun clockAlignment(profileId: Long): ClockAlignment =
        profile(profileId).resolveOverride({ it.overrideClock }, { it.clockAlignment }, globalSettings.clockAlignment)

    /** Independent of [clockAlignment] — see [com.facetlauncher.app.data.model.LauncherSettings.calendarAlignment]. */
    fun calendarAlignment(profileId: Long): ClockAlignment =
        profile(profileId).resolveOverride({ it.overrideClock }, { it.calendarAlignment }, globalSettings.calendarAlignment)

    fun clockZoneHeightDp(profileId: Long): Float? =
        profile(profileId).resolveOverride({ it.overrideClock }, { it.clockZoneHeightDp }, globalSettings.clockZoneHeightDp)

    fun clockScale(profileId: Long): Float =
        profile(profileId).resolveOverride({ it.overrideClock }, { it.clockScale }, globalSettings.clockScale)

    /** Mirrors [com.facetlauncher.app.ui.home.HomeUiState.activeCalendarFontOption]'s own gating exactly — same `overrideCalendar` flag. */
    fun calendarFontOption(profileId: Long): ClockFontOption =
        profile(profileId)?.let { if (it.overrideCalendar) it.calendarFontOption else globalSettings.calendarFontOption } ?: globalSettings.calendarFontOption

    fun calendarColorOption(profileId: Long): ClockColorOption =
        profile(profileId)?.let { if (it.overrideCalendar) it.calendarColorOption else globalSettings.calendarColorOption } ?: globalSettings.calendarColorOption

    fun calendarFontWeight(profileId: Long): FontWeightOption =
        profile(profileId)?.let { if (it.overrideCalendar) it.calendarFontWeight else globalSettings.calendarFontWeight } ?: globalSettings.calendarFontWeight

    /** Mirrors [com.facetlauncher.app.ui.home.HomeUiState.activeAppRowPosition]'s own gating exactly — same `overrideApps` flag. */
    fun appRowPosition(profileId: Long): AppRowPosition =
        profile(profileId)?.let { if (it.overrideApps) it.appRowPosition else globalSettings.appRowPosition } ?: globalSettings.appRowPosition

    fun appRowPresentation(profileId: Long): AppRowPresentation =
        profile(profileId)?.let { if (it.overrideApps) it.appRowPresentation else globalSettings.appRowPresentation } ?: globalSettings.appRowPresentation

    fun listContentMode(profileId: Long): ListContentMode =
        profile(profileId)?.let { if (it.overrideApps) it.listContentMode else globalSettings.listContentMode } ?: globalSettings.listContentMode

    /** Mirrors [com.facetlauncher.app.ui.home.HomeUiState.activeDockDisplayMode]'s own gating — same `overrideDock` flag. The dock's app list itself comes from each profile's [ProfilePreviewData.dockApps]. */
    fun dockDisplayMode(profileId: Long): DockDisplayMode =
        profile(profileId)?.let { if (it.overrideDock) it.dockDisplayMode else globalSettings.dockDisplayMode } ?: globalSettings.dockDisplayMode
}

@HiltViewModel
class ProfileCarouselViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val wallpaperRepository: WallpaperRepository,
    observeProfilePreviews: ObserveProfilePreviewsUseCase,
) : ViewModel() {

    private val homeWallpaper = MutableStateFlow<HomeWallpaper>(HomeWallpaper.Unavailable)

    val uiState: StateFlow<ProfileCarouselUiState> = combine(
        profileRepository.observeProfiles(),
        settingsRepository.settings,
        observeProfilePreviews(),
        homeWallpaper,
    ) { profiles, settings, previewsByProfileId, wallpaper ->
        ProfileCarouselUiState(
            profiles = profiles,
            activeProfileId = settings.activeProfileId,
            previewsByProfileId = previewsByProfileId,
            globalSettings = settings,
            homeWallpaper = wallpaper,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileCarouselUiState())

    init {
        viewModelScope.launch { homeWallpaper.value = wallpaperRepository.currentHomeWallpaper() }
    }

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
}
