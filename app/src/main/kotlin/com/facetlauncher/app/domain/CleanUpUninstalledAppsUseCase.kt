package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FolderRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.collect

/**
 * Permanently deletes an app's Favorites (every facet, plus the launcher-wide default list),
 * Dock entries (the launcher-wide default dock, plus every facet's own), and folder membership
 * (every folder in the library, whether or not it's currently placed anywhere) the moment it's
 * genuinely uninstalled — as opposed to
 * [FavoriteAppRepository]/[DockAppRepository]/[FolderRepository]'s own runtime filtering, which
 * only hides a missing app from view without touching its stored row, so a later reinstall of the
 * same app would otherwise silently restore its old slot/membership. Runs for as long as it's
 * collected — launched once, for the app's lifetime, from
 * [com.facetlauncher.app.ui.launcher.LauncherViewModel].
 */
class CleanUpUninstalledAppsUseCase @Inject constructor(
    private val appRepository: AppRepository,
    private val dockAppRepository: DockAppRepository,
    private val facetDockAppRepository: FacetDockAppRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val folderRepository: FolderRepository,
) {
    suspend operator fun invoke() {
        appRepository.observeUninstalledPackages().collect { packageName ->
            dockAppRepository.removeByPackage(packageName)
            facetDockAppRepository.removeByPackage(packageName)
            favoriteAppRepository.removeByPackage(packageName)
            defaultFavoriteAppRepository.removeByPackage(packageName)
            folderRepository.removeByPackage(packageName)
        }
    }
}
