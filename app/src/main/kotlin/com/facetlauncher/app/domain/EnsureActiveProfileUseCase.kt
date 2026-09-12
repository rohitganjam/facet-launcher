package com.facetlauncher.app.domain

import com.facetlauncher.app.data.ProfileRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.NO_ACTIVE_PROFILE_ID
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Bootstraps profile state on app start (F4: the launcher starts with exactly one profile).
 * Spans [ProfileRepository] and [SettingsRepository], so it's a UseCase rather than living in
 * either repository or a ViewModel.
 */
class EnsureActiveProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke() {
        val profiles = profileRepository.observeProfiles().first()
        val activeId = settingsRepository.settings.first().activeProfileId

        val validActiveProfile = if (activeId != NO_ACTIVE_PROFILE_ID) {
            profiles.find { it.id == activeId }
        } else {
            null
        }
        if (validActiveProfile != null) return

        val resolvedProfile = profiles.firstOrNull() ?: profileRepository.addProfile()
        settingsRepository.setActiveProfileId(resolvedProfile.id)
    }
}
