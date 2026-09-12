package com.facetlauncher.app.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.ProfileRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.ProfileEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ManageProfilesUiState(
    val profiles: List<ProfileEntity> = emptyList(),
    val activeProfileId: Long = 0L,
) {
    val canAddProfile: Boolean get() = profiles.size < ProfileRepository.MAX_PROFILES
    val canDeleteProfile: Boolean get() = profiles.size > ProfileRepository.MIN_PROFILES
}

/**
 * Settings → Profiles ("Manage Profiles"): the drag-to-reorder / add / delete list. A plain
 * settings screen — unlike [ProfileCarouselViewModel], it only needs the profile list, so it
 * skips that class's expensive per-profile preview flow ([com.facetlauncher.app.domain.ObserveProfilePreviewsUseCase]).
 */
@HiltViewModel
class ManageProfilesViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<ManageProfilesUiState> = combine(
        profileRepository.observeProfiles(),
        settingsRepository.settings,
    ) { profiles, settings ->
        ManageProfilesUiState(profiles = profiles, activeProfileId = settings.activeProfileId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ManageProfilesUiState())

    fun reorderProfiles(orderedProfiles: List<ProfileEntity>) {
        viewModelScope.launch { profileRepository.reorderProfiles(orderedProfiles) }
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
                uiState.value.profiles.firstOrNull { it.id != profile.id }
                    ?.let { settingsRepository.setActiveProfileId(it.id) }
            }
        }
    }
}
