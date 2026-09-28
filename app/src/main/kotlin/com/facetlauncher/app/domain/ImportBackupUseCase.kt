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
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.BackupSettings
import com.facetlauncher.app.data.model.BackupWidgetPlacement
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.CURRENT_BACKUP_VERSION
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.DrawerFolderDisplayMode
import com.facetlauncher.app.data.model.DrawerGridSize
import com.facetlauncher.app.data.model.DrawerListItemSize
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.SearchBarPosition
import com.facetlauncher.app.data.model.ThemeMode
import com.facetlauncher.app.data.model.WallpaperAccentRole
import javax.inject.Inject

sealed interface ImportBackupResult {
    data class Success(
        val facetCount: Int,
        val dockAppCount: Int,
        val defaultFavoriteCount: Int,
        /** Never auto-restored — see [com.facetlauncher.app.data.model.BackupWidgetPlacement]'s own doc comment. Empty when the backup had none. */
        val pendingWidgetPlacements: List<BackupWidgetPlacement>,
    ) : ImportBackupResult

    /** [uri] wasn't valid JSON, or wasn't shaped like a backup at all. */
    data object InvalidFile : ImportBackupResult

    /** From a newer app version than this one understands — refuse rather than silently dropping fields it doesn't recognize. */
    data class UnsupportedVersion(val backupVersion: Int) : ImportBackupResult
}

/**
 * F14 Backup & Restore import — replaces every current facet/dock/default-favorite with the
 * backup's own (a destructive, full-replace restore, not a merge — the caller must confirm this
 * with the user before invoking; see `BackupRestoreScreen.kt`), then applies every setting field.
 * Widget placements are surfaced back to the caller rather than written automatically — see
 * [ImportBackupResult.Success.pendingWidgetPlacements].
 *
 * Not wrapped in a single Room transaction (a failure partway leaves a partially-restored state)
 * — a deliberate scope trade-off for a rare, user-initiated, already-confirmed operation, not an
 * oversight; revisit if this ever needs a stronger guarantee.
 */
class ImportBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val dockAppRepository: DockAppRepository,
    private val facetDockAppRepository: FacetDockAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val folderRepository: FolderRepository,
    private val repairOrphanedProfileRows: RepairOrphanedProfileRowsUseCase,
) {
    suspend operator fun invoke(uri: Uri): ImportBackupResult {
        val bundle = backupRepository.readBackup(uri) ?: return ImportBackupResult.InvalidFile
        if (bundle.backupVersion > CURRENT_BACKUP_VERSION) return ImportBackupResult.UnsupportedVersion(bundle.backupVersion)

        applySettings(bundle.settings)

        // Folders are restored first, before anything that places them — deleteAllFolders also
        // wipes every placement table via FK cascade, so this alone clears the old folder state.
        folderRepository.deleteAllFolders()
        val newFolderIdByIndex = bundle.folders.mapIndexed { index, backupFolder ->
            index to folderRepository.restoreFolder(backupFolder.toFolderEntity(), backupFolder.apps.map { it.toFolderAppEntity() })
        }.toMap()

        facetRepository.deleteAllFacets()
        var activeFacetId: Long? = null
        bundle.facets.forEachIndexed { index, backupFacet ->
            val newId = facetRepository.restoreFacet(backupFacet.toFacetEntity())
            backupFacet.favorites.forEach { entry ->
                favoriteAppRepository.restoreFavorite(entry.toFavoriteAppEntity(newId))
            }
            backupFacet.dockApps.forEach { entry ->
                facetDockAppRepository.restoreDockApp(entry.toFacetDockAppEntity(newId))
            }
            backupFacet.dockFolderPlacements.forEach { placement ->
                newFolderIdByIndex[placement.folderIndex]?.let { folderId ->
                    facetDockAppRepository.restoreDockFolderPlacement(newId, folderId, placement.position)
                }
            }
            backupFacet.favoriteFolderPlacements.forEach { placement ->
                newFolderIdByIndex[placement.folderIndex]?.let { folderId ->
                    favoriteAppRepository.restoreFavoriteFolderPlacement(newId, folderId, placement.position)
                }
            }
            if (index == bundle.settings.activeFacetIndex) activeFacetId = newId
        }
        activeFacetId?.let { settingsRepository.setActiveFacetId(it) }

        dockAppRepository.deleteAllDockApps()
        bundle.dockApps.forEach { dockAppRepository.restoreDockApp(it.toDockAppEntity()) }
        bundle.dockFolderPlacements.forEach { placement ->
            newFolderIdByIndex[placement.folderIndex]?.let { folderId ->
                dockAppRepository.restoreDockFolderPlacement(folderId, placement.position)
            }
        }

        defaultFavoriteAppRepository.deleteAllDefaultFavorites()
        bundle.defaultFavoriteApps.forEach { defaultFavoriteAppRepository.restoreDefaultFavorite(it.toDefaultFavoriteAppEntity()) }
        bundle.defaultFavoriteFolderPlacements.forEach { placement ->
            newFolderIdByIndex[placement.folderIndex]?.let { folderId ->
                defaultFavoriteAppRepository.restoreDefaultFavoriteFolderPlacement(folderId, placement.position)
            }
        }

        // BackupAppEntry has no portable userId (see BackupBundle's own doc comment), so every
        // restored row lands at the -1 orphaned sentinel and won't hydrate against installed apps
        // until resolved — same repair LauncherViewModel.init runs once per app launch, but run
        // here too so restored apps/folders show up immediately instead of needing a restart.
        repairOrphanedProfileRows()

        return ImportBackupResult.Success(
            facetCount = bundle.facets.size,
            dockAppCount = bundle.dockApps.size,
            defaultFavoriteCount = bundle.defaultFavoriteApps.size,
            pendingWidgetPlacements = bundle.widgetPlacements,
        )
    }

    private suspend fun applySettings(settings: BackupSettings) {
        with(settingsRepository) {
            setUse24HourTime(settings.use24HourTime)
            setDockDisplayMode(settings.dockDisplayMode.toEnumOrDefault(DockDisplayMode.ICONS))
            setDrawerPresentation(settings.drawerPresentation.toEnumOrDefault(DrawerPresentation.LIST))
            setDrawerGridSize(settings.drawerGridSize.toEnumOrDefault(DrawerGridSize.FIVE_BY_SIX))
            setDrawerListItemSize(settings.drawerListItemSize.toEnumOrDefault(DrawerListItemSize.REGULAR))
            setDrawerOpacity(settings.drawerOpacity)
            setNotificationDotsEnabled(settings.notificationDotsEnabled)
            setNotificationBadgeStyle(settings.notificationBadgeStyle.toEnumOrDefault(NotificationBadgeStyle.DOT))
            setShowDrawerIcons(settings.showDrawerIcons)
            setShowDrawerLabels(settings.showDrawerLabels)
            setSearchBarPosition(settings.searchBarPosition.toEnumOrDefault(SearchBarPosition.TOP))
            setDrawerFolderDisplayMode(settings.drawerFolderDisplayMode.toEnumOrDefault(DrawerFolderDisplayMode.DO_NOT_SHOW))
            setShowAllDayEvents(settings.showAllDayEvents)
            setSearchContactsEnabled(settings.searchContactsEnabled)
            setThemeMode(settings.themeMode.toEnumOrDefault(ThemeMode.SYSTEM))
            setAccentFromSystem(settings.accentFromSystem)
            settings.customAccentSwatch?.let { setCustomAccentSwatch(it) }
            setWallpaperAccentRole(settings.wallpaperAccentRole.toEnumOrDefault(WallpaperAccentRole.PRIMARY))
            setIconRenderMode(settings.iconRenderMode.toEnumOrDefault(IconRenderMode.SYSTEM_DEFAULT))
            setLauncherFontOption(settings.launcherFontOption.toEnumOrDefault(LauncherFontOption.SYSTEM))
            setFontScaleOption(settings.fontScaleOption.toEnumOrDefault(FontScaleOption.DEFAULT))
            setAppLabelColorOption(settings.appLabelColorOption.toEnumOrDefault(ClockColorOption.THEME))
            setAppRowPosition(settings.appRowPosition.toEnumOrDefault(AppRowPosition.LEFT))
            setAppRowPresentation(settings.appRowPresentation.toEnumOrDefault(AppRowPresentation.ICON_AND_TEXT))
            setAppListLayout(settings.appListLayout.toEnumOrDefault(AppListLayout.SINGLE_COLUMN))
            setAppListColumnAlignment(settings.appListColumnAlignment.toEnumOrDefault(AppListColumnAlignment.BOTH_LEFT))
            setAppListGridColumns(settings.appListGridColumns.toEnumOrDefault(AppListGridColumns.FOUR))
            setAppListGridDisplayMode(settings.appListGridDisplayMode.toEnumOrDefault(AppListGridDisplayMode.ICONS))
            setListContentMode(settings.listContentMode.toEnumOrDefault(ListContentMode.FAVORITES))
            setAppsToShowCount(settings.appsToShowCount)
            setClockTemplateId(settings.clockTemplateId.toEnumOrDefault(ClockTemplateId.LIGHT_STACK))
            setClockFontOption(settings.clockFontOption.toEnumOrDefault(ClockFontOption.LAUNCHER_DEFAULT))
            setClockColorOption(settings.clockColorOption.toEnumOrDefault(ClockColorOption.THEME))
            setClockShowMeridiem(settings.clockShowMeridiem)
            setHomeAppsFontWeight(settings.homeAppsFontWeight.toEnumOrDefault(FontWeightOption.REGULAR))
        }
    }
}
