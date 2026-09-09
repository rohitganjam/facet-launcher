package com.lumenlauncher.app.ui.profiles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.ClockAlignment
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockDateStyle
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.data.model.ListContentMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileSettingsUiState(
    val profile: ProfileEntity? = null,
    /** This profile's own favorites — only meaningful/used while [isOverridingApps] is true. */
    val favorites: List<AppInfo> = emptyList(),
    /** The launcher-wide default Favorites list — shown read-only while inheriting. */
    val defaultFavorites: List<AppInfo> = emptyList(),
    val globalClockTemplateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    val globalClockFontOption: ClockFontOption = ClockFontOption.SYSTEM,
    val globalClockColorOption: ClockColorOption = ClockColorOption.THEME,
    val globalClockAccentColorOption: ClockColorOption = ClockColorOption.ACCENT_PRIMARY,
    val globalUse24HourTime: Boolean = false,
    val globalClockShowMeridiem: Boolean = false,
    val globalClockDateStyle: ClockDateStyle = ClockDateStyle.FULL,
    /** The calendar events strip's own font/color — part of the same Clock+Calendar design block/override, not a separate one (see chat history). */
    val globalCalendarFontOption: ClockFontOption = ClockFontOption.LAUNCHER_DEFAULT,
    val globalCalendarColorOption: ClockColorOption = ClockColorOption.THEME,
    val globalCalendarFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    /** Part of the same Clock+Calendar design bundle as the fields above — see chat history: moved in from being global-only. */
    val globalClockAlignment: ClockAlignment = ClockAlignment.LEFT,
    val globalCalendarAlignment: ClockAlignment = ClockAlignment.LEFT,
    val globalClockZoneHeightDp: Float? = null,
    val globalAppRowPosition: AppRowPosition = AppRowPosition.LEFT,
    val globalAppRowPresentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    val globalListContentMode: ListContentMode = ListContentMode.FAVORITES,
    val globalAppsToShowCount: Int = 5,
) {
    /** The Clock card's single inherit/override switch — governs template, font, color, 24h, and meridiem. */
    val isOverridingClock: Boolean get() = profile?.overrideClock ?: false
    val clockTemplateId: ClockTemplateId get() = if (isOverridingClock) profile?.clockTemplateId ?: globalClockTemplateId else globalClockTemplateId
    val clockFontOption: ClockFontOption get() = if (isOverridingClock) profile?.clockFontOption ?: globalClockFontOption else globalClockFontOption
    val clockColorOption: ClockColorOption get() = if (isOverridingClock) profile?.clockColorOption ?: globalClockColorOption else globalClockColorOption
    val clockAccentColorOption: ClockColorOption get() = if (isOverridingClock) profile?.clockAccentColorOption ?: globalClockAccentColorOption else globalClockAccentColorOption
    val effectiveUse24HourTime: Boolean get() = if (isOverridingClock) profile?.use24HourTime ?: globalUse24HourTime else globalUse24HourTime
    val clockShowMeridiem: Boolean get() = if (isOverridingClock) profile?.clockShowMeridiem ?: globalClockShowMeridiem else globalClockShowMeridiem
    val clockDateStyle: ClockDateStyle get() = if (isOverridingClock) profile?.clockDateStyle ?: globalClockDateStyle else globalClockDateStyle
    val calendarFontOption: ClockFontOption get() = if (isOverridingClock) profile?.calendarFontOption ?: globalCalendarFontOption else globalCalendarFontOption
    val calendarColorOption: ClockColorOption get() = if (isOverridingClock) profile?.calendarColorOption ?: globalCalendarColorOption else globalCalendarColorOption
    val calendarFontWeight: FontWeightOption get() = if (isOverridingClock) profile?.calendarFontWeight ?: globalCalendarFontWeight else globalCalendarFontWeight
    val clockAlignment: ClockAlignment get() = if (isOverridingClock) profile?.clockAlignment ?: globalClockAlignment else globalClockAlignment
    val calendarAlignment: ClockAlignment get() = if (isOverridingClock) profile?.calendarAlignment ?: globalCalendarAlignment else globalCalendarAlignment
    val clockZoneHeightDp: Float? get() = if (isOverridingClock) profile?.clockZoneHeightDp ?: globalClockZoneHeightDp else globalClockZoneHeightDp

    /** The Apps card's single inherit/override switch — governs position, presentation, list content mode, apps-to-show, and favorites together. */
    val isOverridingApps: Boolean get() = profile?.overrideApps ?: false
    val appRowPosition: AppRowPosition get() = if (isOverridingApps) profile?.appRowPosition ?: globalAppRowPosition else globalAppRowPosition
    val appRowPresentation: AppRowPresentation get() = if (isOverridingApps) profile?.appRowPresentation ?: globalAppRowPresentation else globalAppRowPresentation
    val listContentMode: ListContentMode get() = if (isOverridingApps) profile?.listContentMode ?: globalListContentMode else globalListContentMode
    val appsToShowCount: Int get() = if (isOverridingApps) profile?.appsToShowCount ?: globalAppsToShowCount else globalAppsToShowCount
    val effectiveFavorites: List<AppInfo> get() = if (profile?.overridingFavorites == true) favorites else defaultFavorites
    val favoritesLabel: String get() = "${effectiveFavorites.size} of ${FavoriteAppRepository.MAX_FAVORITES}"
}

@HiltViewModel
class ProfileSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
) : ViewModel() {

    val profileId: Long = checkNotNull(savedStateHandle["profileId"])

    val uiState: StateFlow<ProfileSettingsUiState> = combine(
        profileRepository.observeProfiles(),
        favoriteAppRepository.observeFavoritesForProfile(profileId),
        defaultFavoriteAppRepository.observeDefaultFavorites(),
        settingsRepository.settings,
    ) { profiles, favorites, defaultFavorites, settings ->
        ProfileSettingsUiState(
            profile = profiles.find { it.id == profileId },
            favorites = favorites,
            defaultFavorites = defaultFavorites,
            globalClockTemplateId = settings.clockTemplateId,
            globalClockFontOption = settings.clockFontOption,
            globalClockColorOption = settings.clockColorOption,
            globalClockAccentColorOption = settings.clockAccentColorOption,
            globalUse24HourTime = settings.use24HourTime,
            globalClockShowMeridiem = settings.clockShowMeridiem,
            globalClockDateStyle = settings.clockDateStyle,
            globalCalendarFontOption = settings.calendarFontOption,
            globalCalendarColorOption = settings.calendarColorOption,
            globalCalendarFontWeight = settings.calendarFontWeight,
            globalClockAlignment = settings.clockAlignment,
            globalCalendarAlignment = settings.calendarAlignment,
            globalClockZoneHeightDp = settings.clockZoneHeightDp,
            globalAppRowPosition = settings.appRowPosition,
            globalAppRowPresentation = settings.appRowPresentation,
            globalListContentMode = settings.listContentMode,
            globalAppsToShowCount = settings.appsToShowCount,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileSettingsUiState())

    fun renameProfile(newName: String) {
        val profile = uiState.value.profile ?: return
        viewModelScope.launch { profileRepository.renameProfile(profile, newName) }
    }

    /** Switching to Override seeds the profile's stored value with the current effective one; switching back to Inherit clears it. */
    fun setOverridingClock(overriding: Boolean) {
        val profile = uiState.value.profile ?: return
        val state = uiState.value
        viewModelScope.launch {
            profileRepository.updateOverridingClock(
                profile = profile,
                overriding = overriding,
                templateId = state.clockTemplateId,
                fontOption = state.clockFontOption,
                colorOption = state.clockColorOption,
                use24HourTime = state.effectiveUse24HourTime,
                showMeridiem = state.clockShowMeridiem,
                calendarFontOption = state.calendarFontOption,
                calendarColorOption = state.calendarColorOption,
                calendarFontWeight = state.calendarFontWeight,
                clockAlignment = state.clockAlignment,
                calendarAlignment = state.calendarAlignment,
                clockZoneHeightDp = state.clockZoneHeightDp,
                accentColorOption = state.clockAccentColorOption,
                dateStyle = state.clockDateStyle,
            )
        }
    }

    /**
     * The Apps card's single Inherit/Override switch — governs position, presentation, list
     * content mode, apps-to-show, and favorites as one unit. Switching to Override seeds all of
     * them with current effective values (including copying the default favorites list if the
     * profile's own list is empty) so the card doesn't suddenly go empty.
     */
    fun setOverridingApps(overriding: Boolean) {
        val profile = uiState.value.profile ?: return
        val state = uiState.value
        viewModelScope.launch {
            if (overriding && state.favorites.isEmpty()) {
                favoriteAppRepository.replaceFavorites(profileId, state.defaultFavorites)
            }
            profileRepository.updateOverridingApps(
                profile = profile,
                overriding = overriding,
                position = state.appRowPosition,
                presentation = state.appRowPresentation,
                mode = state.listContentMode,
                count = state.appsToShowCount,
                overridingFavorites = overriding,
            )
        }
    }

    fun setAppRowPosition(position: AppRowPosition) {
        val profile = uiState.value.profile ?: return
        viewModelScope.launch { profileRepository.setAppRowPosition(profile, position) }
    }

    fun setAppRowPresentation(presentation: AppRowPresentation) {
        val profile = uiState.value.profile ?: return
        viewModelScope.launch { profileRepository.setAppRowPresentation(profile, presentation) }
    }

    fun setListContentMode(mode: ListContentMode) {
        val profile = uiState.value.profile ?: return
        viewModelScope.launch { profileRepository.setListContentMode(profile, mode) }
    }

    fun setAppsToShowCount(count: Int) {
        val profile = uiState.value.profile ?: return
        viewModelScope.launch { profileRepository.setAppsToShowCount(profile, count) }
    }

    /** Drag-reorder only — adding/removing favorites happens on the picker screen, not here. */
    fun reorderFavorites(orderedApps: List<AppInfo>) {
        viewModelScope.launch { favoriteAppRepository.reorderFavorites(profileId, orderedApps) }
    }
}
