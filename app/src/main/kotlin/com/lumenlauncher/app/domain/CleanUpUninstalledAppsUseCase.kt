package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.AppRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.collect

/**
 * Permanently deletes an app's Favorites (every profile, plus the launcher-wide default list)
 * and Dock entries the moment it's genuinely uninstalled — as opposed to
 * [FavoriteAppRepository]/[DockAppRepository]'s own runtime filtering, which only hides a missing
 * app from view without touching its stored row, so a later reinstall of the same app would
 * otherwise silently restore its old slot. Runs for as long as it's collected — launched once,
 * for the app's lifetime, from [com.lumenlauncher.app.ui.launcher.LauncherViewModel].
 */
class CleanUpUninstalledAppsUseCase @Inject constructor(
    private val appRepository: AppRepository,
    private val dockAppRepository: DockAppRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
) {
    suspend operator fun invoke() {
        appRepository.observeUninstalledPackages().collect { packageName ->
            dockAppRepository.removeByPackage(packageName)
            favoriteAppRepository.removeByPackage(packageName)
            defaultFavoriteAppRepository.removeByPackage(packageName)
        }
    }
}
