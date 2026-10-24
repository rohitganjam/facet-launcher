package com.facetlauncher.app.domain

import android.net.Uri
import com.facetlauncher.app.data.BackupRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WidgetPlacementRepository
import com.facetlauncher.app.data.model.BackupBundle
import com.facetlauncher.app.data.model.BackupSettings
import com.facetlauncher.app.data.model.BackupWeatherCity
import com.facetlauncher.app.data.model.LauncherSettings
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.data.model.PlacementList

/**
 * F14 Backup & Restore export — composes every settings/data `Repository` into one [BackupBundle]
 * and hands it to [BackupRepository] to write. See [BackupBundle]'s own doc comment for exactly
 * what is and isn't included and why.
 */
class ExportBackupUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val placementRepository: PlacementRepository,
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
            val favoritesTarget = PlacementTarget(PlacementList.FAVORITES, facet.id)
            val dockTarget = PlacementTarget(PlacementList.DOCK, facet.id)
            val favorites = placementRepository.getRawApps(favoritesTarget).map { it.toBackupEntry() }
            val dockApps = placementRepository.getRawApps(dockTarget).map { it.toBackupEntry() }
            val dockFolderPlacements = placementRepository.getRawFolderPlacements(dockTarget)
                .mapNotNull { it.toBackupPlacement(folderIndexById) }
            val favoriteFolderPlacements = placementRepository.getRawFolderPlacements(favoritesTarget)
                .mapNotNull { it.toBackupPlacement(folderIndexById) }
            facet.toBackupFacet(favorites, dockApps, dockFolderPlacements, favoriteFolderPlacements)
        }

        val bundle = BackupBundle(
            exportedAtEpochMillis = System.currentTimeMillis(),
            settings = settings.toBackupSettings(activeFacetIndex),
            facets = backupFacets,
            dockApps = placementRepository.getRawApps(PlacementTarget.GlobalDock).map { it.toBackupEntry() },
            defaultFavoriteApps = placementRepository.getRawApps(PlacementTarget.GlobalFavorites).map { it.toBackupEntry() },
            widgetPlacements = widgetPlacementRepository.observeAll().first().map { it.toBackupPlacement() },
            folders = backupFolders,
            dockFolderPlacements = placementRepository.getRawFolderPlacements(PlacementTarget.GlobalDock).mapNotNull { it.toBackupPlacement(folderIndexById) },
            defaultFavoriteFolderPlacements = placementRepository.getRawFolderPlacements(PlacementTarget.GlobalFavorites)
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
    drawerGridSize = drawerGridDensity.name,
    drawerListItemSize = drawerListItemSize.name,
    drawerOpacity = drawerOpacity,
    notificationDotsEnabled = notificationDotsEnabled,
    notificationBadgeStyle = notificationBadgeStyle.name,
    showMediaPlayerOnHome = showMediaPlayerOnHome,
    showMediaSkipButtons = showMediaSkipButtons,
    showNotificationsInAppMenu = showNotificationsInAppMenu,
    showDrawerIcons = showDrawerIcons,
    showDrawerLabels = showDrawerLabels,
    searchBarPosition = searchBarPosition.name,
    drawerFolderDisplayMode = drawerFolderDisplayMode.name,
    activeFacetIndex = activeFacetIndex,
    showAllDayEvents = showAllDayEvents,
    showCalendarIndicator = showCalendarIndicator,
    calendarItemsToShow = calendarItemsToShow,
    searchContactsEnabled = searchContactsEnabled,
    themeMode = themeMode.name,
    systemBarIconStyle = systemBarIconStyle.name,
    accentFromSystem = accentFromSystem,
    customAccentSwatch = customAccentSwatch,
    wallpaperAccentRole = wallpaperAccentRole.name,
    iconRenderMode = iconRenderMode.name,
    iconShape = iconShape.name,
    launcherFontOption = launcherFontOption.name,
    fontScaleOption = fontScaleOption.name,
    appLabelColorOption = appLabelColorOption.name,
    appRowPosition = appRowPosition.name,
    appRowPresentation = appRowPresentation.name,
    appListLayout = appListLayout.name,
    appListColumnAlignment = appListColumnAlignment.name,
    landscapeAppRowPosition = landscape.appRowPosition?.name,
    landscapeAppListColumnAlignment = landscape.appListColumnAlignment?.name,
    appListGridColumns = appListGridColumns.name,
    appListGridDisplayMode = appListGridDisplayMode.name,
    listContentMode = listContentMode.name,
    appsToShowCount = appsToShowCount,
    clockTemplateId = clockTemplateId.name,
    clockFontOption = clockFontOption.name,
    clockColorOption = clockColorOption.name,
    clockShowMeridiem = clockShowMeridiem,
    homeAppsFontWeight = homeAppsFontWeight.name,
    recentlyInstalledPosition = recentlyInstalledPosition.name,
    searchSettingsEnabled = searchSettingsEnabled,
    clockAccentColorOption = clockAccentColorOption.name,
    clockDateStyle = clockDateStyle.name,
    clockAlignment = clockAlignment.name,
    clockZoneHeightDp = clockZoneHeightDp,
    clockScale = clockScale,
    appListVerticalAlignment = appListVerticalAlignment.name,
    landscapeClockAlignment = landscape.clockAlignment?.name,
    landscapeClockZoneHeightDp = landscape.clockZoneHeightDp,
    landscapeClockScale = landscape.clockScale,
    landscapeAppListVerticalAlignment = landscape.appListVerticalAlignment?.name,
).withAccessoriesAndWeather(this)

/** The clock accessory switches and weather options; the chosen city goes out as four flat fields, the current location never. */
private fun BackupSettings.withAccessoriesAndWeather(settings: LauncherSettings): BackupSettings = copy(
    showNextAlarm = settings.showNextAlarm,
    showBatteryPercentage = settings.showBatteryPercentage,
    showWeather = settings.showWeather,
    temperatureUnit = settings.temperatureUnit.name,
    windSpeedUnit = settings.windSpeedUnit.name,
    rainUnit = settings.rainUnit.name,
    pressureUnit = settings.pressureUnit.name,
    weatherLocationMode = settings.weatherLocationMode.name,
    weatherCityName = settings.weatherCity?.name,
    weatherCityLatitude = settings.weatherCity?.latitude,
    weatherCityLongitude = settings.weatherCity?.longitude,
    weatherCityTimezone = settings.weatherCity?.timezone,
    savedWeatherCities = settings.savedWeatherCities.map { BackupWeatherCity(it.name, it.latitude, it.longitude, it.timezone) },
)
