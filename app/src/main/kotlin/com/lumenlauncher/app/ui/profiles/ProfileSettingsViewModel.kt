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
    val globalUse24HourTime: Boolean = false,
    val globalListContentMode: ListContentMode = ListContentMode.FAVORITES,
    val globalAppsToShowCount: Int = 5,
) {
    /** The Clock card's single inherit/override switch — governs both Clock style and 24-hour time. */
    val isOverridingClock: Boolean get() = profile?.use24HourTimeOverride != null
    val effectiveUse24HourTime: Boolean get() = profile?.use24HourTimeOverride ?: globalUse24HourTime

    /** The Apps card's single inherit/override switch — governs list content mode, apps-to-show, and favorites together. */
    val isOverridingApps: Boolean get() = profile?.listContentModeOverride != null
    val listContentMode: ListContentMode get() = profile?.listContentModeOverride ?: globalListContentMode
    val appsToShowCount: Int get() = profile?.appsToShowCountOverride ?: globalAppsToShowCount
    val effectiveFavorites: List<AppInfo> get() = if (isOverridingApps) favorites else defaultFavorites
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
            globalUse24HourTime = settings.use24HourTime,
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
        val newOverride = if (overriding) uiState.value.effectiveUse24HourTime else null
        viewModelScope.launch { profileRepository.setUse24HourTimeOverride(profile, newOverride) }
    }

    fun setUse24HourTimeOverride(enabled: Boolean) {
        val profile = uiState.value.profile ?: return
        viewModelScope.launch { profileRepository.setUse24HourTimeOverride(profile, enabled) }
    }

    /**
     * The Apps card's single Inherit/Override switch — governs list content mode, apps-to-show,
     * and favorites as one unit (matching the single card this drives; see `AppsSection` in
     * `ProfileSettingsScreen.kt`). Switching to Override seeds all three with this profile's
     * current effective values (mode/count as-is, favorites as a copy of the default list) so
     * the card doesn't suddenly go empty; switching back to Inherit clears the mode/count
     * override and stops using this profile's own favorites — its own favorites rows are left
     * alone rather than deleted, so they're still there if this profile overrides again later.
     * Delegates the profile-row write to [ProfileRepository.setOverridingApps] as one atomic
     * upsert — see that method's doc for why splitting this into several sequential setter calls
     * (each doing its own full-row replace) doesn't work.
     */
    fun setOverridingApps(overriding: Boolean) {
        val profile = uiState.value.profile ?: return
        val state = uiState.value
        viewModelScope.launch {
            if (overriding) {
                favoriteAppRepository.replaceFavorites(profileId, state.defaultFavorites)
            }
            profileRepository.setOverridingApps(profile, overriding, state.listContentMode, state.appsToShowCount)
        }
    }

    fun setListContentMode(mode: ListContentMode) {
        val profile = uiState.value.profile ?: return
        viewModelScope.launch { profileRepository.setListContentModeOverride(profile, mode) }
    }

    fun setAppsToShowCount(count: Int) {
        val profile = uiState.value.profile ?: return
        viewModelScope.launch { profileRepository.setAppsToShowCountOverride(profile, count) }
    }

    /** Drag-reorder only — adding/removing favorites happens on the picker screen, not here. */
    fun reorderFavorites(orderedApps: List<AppInfo>) {
        viewModelScope.launch { favoriteAppRepository.reorderFavorites(profileId, orderedApps) }
    }
}
