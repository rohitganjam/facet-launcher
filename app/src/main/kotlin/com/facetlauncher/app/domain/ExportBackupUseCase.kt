package com.facetlauncher.app.domain

import android.net.Uri
import com.facetlauncher.app.data.BackupRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.model.BackupBundle
import com.facetlauncher.app.data.model.BackupSettings
import com.facetlauncher.app.data.model.LauncherSettings
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * F14 Backup & Restore export — composes every settings/data `Repository` into one [BackupBundle]
 * and hands it to [BackupRepository] to write. See [BackupBundle]'s own doc comment for exactly
 * what is and isn't included and why.
 */
class ExportBackupUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val dockAppRepository: DockAppRepository,
    private val facetDockAppRepository: FacetDockAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val widgetPlacementRepository: WidgetPlacementRepository,
    private val folderRepository: FolderRepository,
    private val backupRepository: BackupRepository,
) {
    /** Returns the bundle it wrote — mainly so callers (and tests) can inspect exactly what got exported without re-deriving it. */
    suspend operator fun invoke(uri: Uri): BackupBundle {
        val settings = settingsRepository.settings.first()
        val facets = facetRepository.observeFacets().first()
        val activeFacetIndex = facets.indexOfFirst { it.id == settings.activeFacetId }.takeIf { it >= 0 }

        // The whole folder library, exported once, globally — every placement list below
        // references a folder by its index into this list rather than its (non-portable) row id.
        val rawFolders = folderRepository.getRawFolders()
        val folderIndexById = rawFolders.mapIndexed { index, it -> it.folder.id to index }.toMap()
        val backupFolders = rawFolders.map { it.toBackupFolder() }

        val backupFacets = facets.map { facet ->
            val favorites = favoriteAppRepository.getRawFavoritesForFacet(facet.id).map { it.toBackupEntry() }
            val dockApps = facetDockAppRepository.getRawDockAppsForFacet(facet.id).map { it.toBackupEntry() }
            val dockFolderPlacements = facetDockAppRepository.getRawDockFolderPlacementsForFacet(facet.id)
                .mapNotNull { it.toBackupPlacement(folderIndexById) }
            val favoriteFolderPlacements = favoriteAppRepository.getRawFavoriteFolderPlacementsForFacet(facet.id)
                .mapNotNull { it.toBackupPlacement(folderIndexById) }
            facet.toBackupFacet(favorites, dockApps, dockFolderPlacements, favoriteFolderPlacements)
        }

        val bundle = BackupBundle(
            exportedAtEpochMillis = System.currentTimeMillis(),
            settings = settings.toBackupSettings(activeFacetIndex),
            facets = backupFacets,
            dockApps = dockAppRepository.getRawDockApps().map { it.toBackupEntry() },
            defaultFavoriteApps = defaultFavoriteAppRepository.getRawDefaultFavorites().map { it.toBackupEntry() },
            widgetPlacements = widgetPlacementRepository.observeAll().first().map { it.toBackupPlacement() },
            folders = backupFolders,
            dockFolderPlacements = dockAppRepository.getRawDockFolderPlacements().mapNotNull { it.toBackupPlacement(folderIndexById) },
            defaultFavoriteFolderPlacements = defaultFavoriteAppRepository.getRawDefaultFavoriteFolderPlacements()
                .mapNotNull { it.toBackupPlacement(folderIndexById) },
        )
        backupRepository.writeBackup(uri, bundle)
        return bundle
    }
}

private fun LauncherSettings.toBackupSettings(activeFacetIndex: Int?): BackupSettings = BackupSettings(
    use24HourTime = use24HourTime,
    dockDisplayMode = dockDisplayMode.name,
    drawerPresentation = drawerPresentation.name,
    drawerGridSize = drawerGridSize.name,
    drawerListItemSize = drawerListItemSize.name,
    drawerOpacity = drawerOpacity,
    notificationDotsEnabled = notificationDotsEnabled,
    notificationBadgeStyle = notificationBadgeStyle.name,
    showDrawerIcons = showDrawerIcons,
    showDrawerLabels = showDrawerLabels,
    searchBarPosition = searchBarPosition.name,
    drawerFolderDisplayMode = drawerFolderDisplayMode.name,
    activeFacetIndex = activeFacetIndex,
    showAllDayEvents = showAllDayEvents,
    searchContactsEnabled = searchContactsEnabled,
    themeMode = themeMode.name,
    accentFromSystem = accentFromSystem,
    customAccentSwatch = customAccentSwatch,
    wallpaperAccentRole = wallpaperAccentRole.name,
    iconRenderMode = iconRenderMode.name,
    launcherFontOption = launcherFontOption.name,
    appLabelColorOption = appLabelColorOption.name,
    appRowPosition = appRowPosition.name,
    appRowPresentation = appRowPresentation.name,
    listContentMode = listContentMode.name,
    appsToShowCount = appsToShowCount,
    clockTemplateId = clockTemplateId.name,
    clockFontOption = clockFontOption.name,
    clockColorOption = clockColorOption.name,
    clockShowMeridiem = clockShowMeridiem,
    calendarFontOption = calendarFontOption.name,
    calendarColorOption = calendarColorOption.name,
    calendarFontWeight = calendarFontWeight.name,
    homeAppsFontWeight = homeAppsFontWeight.name,
)
