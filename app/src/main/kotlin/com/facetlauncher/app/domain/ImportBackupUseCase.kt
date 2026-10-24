package com.facetlauncher.app.domain

import android.net.Uri
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.BackupRepository
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
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.RecentlyInstalledPosition
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.CURRENT_BACKUP_VERSION
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.DrawerFolderDisplayMode
import com.facetlauncher.app.data.model.DrawerGridDensity
import com.facetlauncher.app.data.model.DrawerListItemSize
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.IconShape
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.PressureUnit
import com.facetlauncher.app.data.model.RainUnit
import com.facetlauncher.app.data.model.SearchBarPosition
import com.facetlauncher.app.data.model.SystemBarIconStyle
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.ThemeMode
import com.facetlauncher.app.data.model.WallpaperAccentRole
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.data.model.WindSpeedUnit
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.weatherCityOrNull
import javax.inject.Inject
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.PlacementTarget

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
    private val placementRepository: PlacementRepository,
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
            val newId = facetRepository.restoreFacet(backupFacet.toFacet())
            val favoritesTarget = PlacementTarget(PlacementList.FAVORITES, newId)
            val dockTarget = PlacementTarget(PlacementList.DOCK, newId)
            backupFacet.favorites.forEach { entry ->
                placementRepository.restoreApp(entry.toAppPlacementEntity(PlacementList.FAVORITES, newId))
            }
            backupFacet.dockApps.forEach { entry ->
                placementRepository.restoreApp(entry.toAppPlacementEntity(PlacementList.DOCK, newId))
            }
            backupFacet.dockFolderPlacements.forEach { placement ->
                newFolderIdByIndex[placement.folderIndex]?.let { folderId ->
                    placementRepository.restoreFolderPlacement(dockTarget, folderId, placement.position)
                }
            }
            backupFacet.favoriteFolderPlacements.forEach { placement ->
                newFolderIdByIndex[placement.folderIndex]?.let { folderId ->
                    placementRepository.restoreFolderPlacement(favoritesTarget, folderId, placement.position)
                }
            }
            if (index == bundle.settings.activeFacetIndex) activeFacetId = newId
        }
        activeFacetId?.let { settingsRepository.setActiveFacetId(it) }

        placementRepository.deleteAll(PlacementTarget.GlobalDock)
        bundle.dockApps.forEach { placementRepository.restoreApp(it.toAppPlacementEntity(PlacementList.DOCK)) }
        bundle.dockFolderPlacements.forEach { placement ->
            newFolderIdByIndex[placement.folderIndex]?.let { folderId ->
                placementRepository.restoreFolderPlacement(PlacementTarget.GlobalDock, folderId, placement.position)
            }
        }

        placementRepository.deleteAll(PlacementTarget.GlobalFavorites)
        bundle.defaultFavoriteApps.forEach { placementRepository.restoreApp(it.toAppPlacementEntity(PlacementList.FAVORITES)) }
        bundle.defaultFavoriteFolderPlacements.forEach { placement ->
            newFolderIdByIndex[placement.folderIndex]?.let { folderId ->
                placementRepository.restoreFolderPlacement(PlacementTarget.GlobalFavorites, folderId, placement.position)
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
            setDrawerGridDensity(DrawerGridDensity.fromStored(settings.drawerGridSize) ?: DrawerGridDensity.COMPACT)
            setDrawerListItemSize(settings.drawerListItemSize.toEnumOrDefault(DrawerListItemSize.REGULAR))
            setDrawerOpacity(settings.drawerOpacity)
            setNotificationDotsEnabled(settings.notificationDotsEnabled)
            setNotificationBadgeStyle(settings.notificationBadgeStyle.toEnumOrDefault(NotificationBadgeStyle.DOT))
            setShowMediaPlayerOnHome(settings.showMediaPlayerOnHome)
            setShowMediaSkipButtons(settings.showMediaSkipButtons)
            setShowNotificationsInAppMenu(settings.showNotificationsInAppMenu)
            setShowDrawerIcons(settings.showDrawerIcons)
            setShowDrawerLabels(settings.showDrawerLabels)
            setSearchBarPosition(settings.searchBarPosition.toEnumOrDefault(SearchBarPosition.TOP))
            setDrawerFolderDisplayMode(settings.drawerFolderDisplayMode.toEnumOrDefault(DrawerFolderDisplayMode.DO_NOT_SHOW))
            setShowAllDayEvents(settings.showAllDayEvents)
            setShowCalendarIndicator(settings.showCalendarIndicator)
            setCalendarItemsToShow(settings.calendarItemsToShow)
            setSearchContactsEnabled(settings.searchContactsEnabled)
            setThemeMode(settings.themeMode.toEnumOrDefault(ThemeMode.SYSTEM))
            setSystemBarIconStyle(settings.systemBarIconStyle.toEnumOrDefault(SystemBarIconStyle.MATCH_THEME))
            setAccentFromSystem(settings.accentFromSystem)
            settings.customAccentSwatch?.let { setCustomAccentSwatch(it) }
            setWallpaperAccentRole(settings.wallpaperAccentRole.toEnumOrDefault(WallpaperAccentRole.PRIMARY))
            setIconRenderMode(settings.iconRenderMode.toEnumOrDefault(IconRenderMode.SYSTEM_DEFAULT))
            setIconShape(settings.iconShape.toEnumOrDefault(IconShape.SQUIRCLE))
            setLauncherFontOption(settings.launcherFontOption.toEnumOrDefault(LauncherFontOption.SYSTEM))
            setFontScaleOption(settings.fontScaleOption.toEnumOrDefault(FontScaleOption.DEFAULT))
            setAppLabelColorOption(settings.appLabelColorOption.toEnumOrDefault(ClockColorOption.THEME))
            setAppRowPosition(settings.appRowPosition.toEnumOrDefault(AppRowPosition.LEFT))
            setAppRowPresentation(settings.appRowPresentation.toEnumOrDefault(AppRowPresentation.ICON_AND_TEXT))
            setAppListLayout(settings.appListLayout.toEnumOrDefault(AppListLayout.SINGLE_COLUMN))
            setAppListColumnAlignment(settings.appListColumnAlignment.toEnumOrDefault(AppListColumnAlignment.BOTH_LEFT))
            setAppListVerticalAlignment(settings.appListVerticalAlignment.toEnumOrDefault(AppListVerticalAlignment.BOTTOM))
            setRecentlyInstalledPosition(settings.recentlyInstalledPosition.toEnumOrDefault(RecentlyInstalledPosition.SHOW_FIRST))
            setSearchSettingsEnabled(settings.searchSettingsEnabled)
            applyClockPlacement(settings)
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

    /** The clock accessory switches and the weather options; the chosen city comes back only when all four of its fields are present. */
    private suspend fun applyAccessoriesAndWeather(settings: BackupSettings) {
        with(settingsRepository) {
            setShowNextAlarm(settings.showNextAlarm)
            setShowBatteryPercentage(settings.showBatteryPercentage)
            setShowWeather(settings.showWeather)
            setTemperatureUnit(settings.temperatureUnit.toEnumOrDefault(TemperatureUnit.AUTO))
            setWindSpeedUnit(settings.windSpeedUnit.toEnumOrDefault(WindSpeedUnit.AUTO))
            setRainUnit(settings.rainUnit.toEnumOrDefault(RainUnit.AUTO))
            setPressureUnit(settings.pressureUnit.toEnumOrDefault(PressureUnit.AUTO))
            setWeatherLocationMode(settings.weatherLocationMode.toEnumOrDefault(WeatherLocationMode.CURRENT))
            setSavedWeatherCities(settings.savedWeatherCities.map { WeatherCity(it.name, it.latitude, it.longitude, it.timezone) })
            setWeatherCity(weatherCityOrNull(settings.weatherCityName, settings.weatherCityLatitude, settings.weatherCityLongitude, settings.weatherCityTimezone))
        }
    }

    /** The clock's look and placement, and the whole landscape group, which a full restore replaces. */
    private suspend fun applyClockPlacement(settings: BackupSettings) {
        with(settingsRepository) {
            setClockAccentColorOption(settings.clockAccentColorOption.toEnumOrDefault(ClockColorOption.ACCENT_PRIMARY))
            setClockDateStyle(settings.clockDateStyle.toEnumOrDefault(ClockDateStyle.FULL))
            applyAccessoriesAndWeather(settings)
            setClockAlignment(settings.clockAlignment.toEnumOrDefault(ClockAlignment.LEFT))
            setClockScale(settings.clockScale)
            // A null height means the divider was never dragged; a restore must clear a height set since.
            if (settings.clockZoneHeightDp != null) setClockZoneHeight(settings.clockZoneHeightDp) else resetClockZoneHeight()
            updateLandscape {
                it.copy(
                    clockAlignment = settings.landscapeClockAlignment?.toEnumOrNull(),
                    clockZoneHeightDp = settings.landscapeClockZoneHeightDp,
                    clockScale = settings.landscapeClockScale,
                    appRowPosition = settings.landscapeAppRowPosition?.toEnumOrNull(),
                    appListVerticalAlignment = settings.landscapeAppListVerticalAlignment?.toEnumOrNull(),
                    appListColumnAlignment = settings.landscapeAppListColumnAlignment?.toEnumOrNull(),
                )
            }
        }
    }
}
