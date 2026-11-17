package com.facetlauncher.app.domain

import com.facetlauncher.app.data.AppVaultRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.LaunchHistoryRepository
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.vaultKey
import javax.inject.Inject

/**
 * Puts apps in the App Vault. The vault entry is written first, so they leave every list at once; then the Favorites, Dock and folder
 * rows they held, in every facet, and their launch history are deleted, so nothing about them is left behind. Taking an app back out
 * of the vault returns it to the drawer only.
 */
class MoveToVaultUseCase @Inject constructor(
    private val vaultRepository: AppVaultRepository,
    private val placementRepository: PlacementRepository,
    private val folderRepository: FolderRepository,
    private val launchHistoryRepository: LaunchHistoryRepository,
) {
    /** How many of [apps] are on Home or in a folder right now, for the confirmation that says they will be taken off. */
    suspend fun placedCount(apps: Collection<AppInfo>): Int = apps.count { app ->
        val userId = app.userHandle.hashCode()
        placementRepository.countByPackage(app.packageName, userId) + folderRepository.countByPackage(app.packageName, userId) > 0
    }

    suspend operator fun invoke(apps: Collection<AppInfo>) {
        vaultRepository.add(apps.map { it.vaultKey })
        apps.forEach { app ->
            val userId = app.userHandle.hashCode()
            placementRepository.removeByPackage(app.packageName, userId)
            folderRepository.removeByPackage(app.packageName, userId)
            launchHistoryRepository.removeByPackage(app.packageName, userId)
        }
    }
}
