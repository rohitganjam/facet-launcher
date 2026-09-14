package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PlacedItem
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class SettingsScreenState(
    val settings: LauncherSettings,
    val dockItems: List<PlacedItem>,
    val defaultFavorites: List<PlacedItem>,
    /** The full folder library's size — a folder is global and independent of the Dock, so this isn't derived from [dockItems]. */
    val folderCount: Int,
)

/**
 * Combines [SettingsRepository], [DockAppRepository], [DefaultFavoriteAppRepository] and
 * [FolderRepository] into one observable state for the Settings screen — the one place in
 * Phase 2 where logic genuinely spans multiple repositories, so it earns a UseCase rather than
 * being called directly from a ViewModel.
 */
class ObserveSettingsScreenStateUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dockAppRepository: DockAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val folderRepository: FolderRepository,
) {
    operator fun invoke(): Flow<SettingsScreenState> =
        combine(
            settingsRepository.settings,
            dockAppRepository.observeDockItems(),
            defaultFavoriteAppRepository.observeDefaultItems(),
            folderRepository.observeFolders(),
        ) { settings, dockItems, defaultFavorites, folders ->
            SettingsScreenState(settings = settings, dockItems = dockItems, defaultFavorites = defaultFavorites, folderCount = folders.size)
        }
}
