package com.facetlauncher.app.domain

import android.net.Uri
import com.facetlauncher.app.data.BackupRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.BackupSettings
import com.facetlauncher.app.data.model.BackupWidgetPlacement
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.CURRENT_BACKUP_VERSION
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.DrawerGridSize
import com.facetlauncher.app.data.model.DrawerListItemSize
import com.facetlauncher.app.data.model.DrawerPresentation
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
) {
    suspend operator fun invoke(uri: Uri): ImportBackupResult {
        val bundle = backupRepository.readBackup(uri) ?: return ImportBackupResult.InvalidFile
        if (bundle.backupVersion > CURRENT_BACKUP_VERSION) return ImportBackupResult.UnsupportedVersion(bundle.backupVersion)

        applySettings(bundle.settings)

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
            if (index == bundle.settings.activeFacetIndex) activeFacetId = newId
        }
        activeFacetId?.let { settingsRepository.setActiveFacetId(it) }

        dockAppRepository.deleteAllDockApps()
        bundle.dockApps.forEach { dockAppRepository.restoreDockApp(it.toDockAppEntity()) }

        defaultFavoriteAppRepository.deleteAllDefaultFavorites()
        bundle.defaultFavoriteApps.forEach { defaultFavoriteAppRepository.restoreDefaultFavorite(it.toDefaultFavoriteAppEntity()) }

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
            setShowAllDayEvents(settings.showAllDayEvents)
            setSearchContactsEnabled(settings.searchContactsEnabled)
            setThemeMode(settings.themeMode.toEnumOrDefault(ThemeMode.SYSTEM))
            setAccentFromSystem(settings.accentFromSystem)
            settings.customAccentSwatch?.let { setCustomAccentSwatch(it) }
            setWallpaperAccentRole(settings.wallpaperAccentRole.toEnumOrDefault(WallpaperAccentRole.PRIMARY))
            setIconRenderMode(settings.iconRenderMode.toEnumOrDefault(IconRenderMode.SYSTEM_DEFAULT))
            setLauncherFontOption(settings.launcherFontOption.toEnumOrDefault(LauncherFontOption.SYSTEM))
            setAppLabelColorOption(settings.appLabelColorOption.toEnumOrDefault(ClockColorOption.THEME))
            setAppRowPosition(settings.appRowPosition.toEnumOrDefault(AppRowPosition.LEFT))
            setAppRowPresentation(settings.appRowPresentation.toEnumOrDefault(AppRowPresentation.ICON_AND_TEXT))
            setListContentMode(settings.listContentMode.toEnumOrDefault(ListContentMode.FAVORITES))
            setAppsToShowCount(settings.appsToShowCount)
            setClockTemplateId(settings.clockTemplateId.toEnumOrDefault(ClockTemplateId.LIGHT_STACK))
            setClockFontOption(settings.clockFontOption.toEnumOrDefault(ClockFontOption.LAUNCHER_DEFAULT))
            setClockColorOption(settings.clockColorOption.toEnumOrDefault(ClockColorOption.THEME))
            setClockShowMeridiem(settings.clockShowMeridiem)
            setCalendarFontOption(settings.calendarFontOption.toEnumOrDefault(ClockFontOption.LAUNCHER_DEFAULT))
            setCalendarColorOption(settings.calendarColorOption.toEnumOrDefault(ClockColorOption.THEME))
            setCalendarFontWeight(settings.calendarFontWeight.toEnumOrDefault(FontWeightOption.REGULAR))
            setHomeAppsFontWeight(settings.homeAppsFontWeight.toEnumOrDefault(FontWeightOption.REGULAR))
        }
    }
}
