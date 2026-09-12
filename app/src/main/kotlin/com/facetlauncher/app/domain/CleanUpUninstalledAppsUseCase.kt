package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.collect

/**
 * Permanently deletes an app's Favorites (every facet, plus the launcher-wide default list)
 * and Dock entries (the launcher-wide default dock, plus every facet's own) the moment it's
 * genuinely uninstalled — as opposed to
 * [FavoriteAppRepository]/[DockAppRepository]'s own runtime filtering, which only hides a missing
 * app from view without touching its stored row, so a later reinstall of the same app would
 * otherwise silently restore its old slot. Runs for as long as it's collected — launched once,
 * for the app's lifetime, from [com.facetlauncher.app.ui.launcher.LauncherViewModel].
 */
class CleanUpUninstalledAppsUseCase @Inject constructor(
    private val appRepository: AppRepository,
    private val dockAppRepository: DockAppRepository,
    private val facetDockAppRepository: FacetDockAppRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
) {
    suspend operator fun invoke() {
        appRepository.observeUninstalledPackages().collect { packageName ->
            dockAppRepository.removeByPackage(packageName)
            facetDockAppRepository.removeByPackage(packageName)
            favoriteAppRepository.removeByPackage(packageName)
            defaultFavoriteAppRepository.removeByPackage(packageName)
        }
    }
}
