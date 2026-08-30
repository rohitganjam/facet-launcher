package com.lumenlauncher.app.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.domain.ObserveProfilePreviewsUseCase
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
    val favoritesByProfileId: Map<Long, List<AppInfo>> = emptyMap(),
    val globalUse24HourTime: Boolean = false,
    // Dock is shared across all profiles (not per-profile — see DockAppRepository), so every
    // preview card renders the same dockApps/dockDisplayMode rather than one entry per profile.
    val dockApps: List<AppInfo> = emptyList(),
    val dockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
) {
    val canAddProfile: Boolean get() = profiles.size < ProfileRepository.MAX_PROFILES
    val canDeleteProfile: Boolean get() = profiles.size > ProfileRepository.MIN_PROFILES

    /** Each profile can override 24-hour time independently — falls back to the global default when unset. */
    fun effectiveUse24HourTime(profileId: Long): Boolean =
        profiles.find { it.id == profileId }?.use24HourTimeOverride ?: globalUse24HourTime
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
    ) { profiles, settings, favoritesByProfileId, dockApps ->
        ProfileCarouselUiState(
            profiles = profiles,
            activeProfileId = settings.activeProfileId,
            favoritesByProfileId = favoritesByProfileId,
            globalUse24HourTime = settings.use24HourTime,
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
