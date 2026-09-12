package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.ProfileDockAppRepository
import com.facetlauncher.app.data.ProfileRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.AppInfo
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * F12's long-press "Add to Dock"/"Add to profile dock" row — mirrors [AddAppToFavoritesUseCase]
 * exactly, but for the Dock (the active profile's own when it's overriding, otherwise the
 * launcher-wide default), matching [ObserveQuickAddStateUseCase]'s own resolution.
 */
class AddAppToDockUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val dockAppRepository: DockAppRepository,
    private val profileDockAppRepository: ProfileDockAppRepository,
) {
    suspend operator fun invoke(app: AppInfo) {
        val activeProfileId = settingsRepository.settings.first().activeProfileId
        val profile = profileRepository.getById(activeProfileId)
        if (profile?.overrideDock == true) {
            val position = profileDockAppRepository.observeDockAppsForProfile(profile.id).first().size
            profileDockAppRepository.addDockApp(profile.id, app, position)
        } else {
            val position = dockAppRepository.observeDockApps().first().size
            dockAppRepository.addDockApp(app, position)
        }
    }
}
