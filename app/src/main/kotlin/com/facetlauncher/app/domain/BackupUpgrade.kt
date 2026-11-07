package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.BackupBundle
import com.facetlauncher.app.data.model.BackupFacet
import com.facetlauncher.app.data.model.BackupSettings
import com.facetlauncher.app.data.model.CURRENT_BACKUP_VERSION
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.ClockCalendarConfig
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.LEGACY_LAUNCHER_DEFAULT
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.data.model.decodeWeatherLocation
import com.facetlauncher.app.data.model.encode
import com.facetlauncher.app.data.model.upgradeLegacyNativeWidgetConfig

/** Backup version that first wrote widget settings as JSON; older ones wrote tab-separated lines. */
private const val FIRST_JSON_CONFIG_VERSION = 4

/** Backup version whose facets carry the clock's own settings; older ones kept them on the facet and the launcher. */
private const val FIRST_CLOCK_CONFIG_VERSION = 5

/** [this] in the current shape, so the import below this point only ever sees one: older backups get their widget, clock and Home settings rewritten. */
fun BackupBundle.upgradedToCurrent(): BackupBundle {
    if (backupVersion >= CURRENT_BACKUP_VERSION) return this
    val withClocks = if (backupVersion < FIRST_CLOCK_CONFIG_VERSION) {
        copy(
            facets = facets.map { it.withUpgradedConfigs(settings, upgradeWidgetConfigs = backupVersion < FIRST_JSON_CONFIG_VERSION) },
            widgetPlacements = if (backupVersion < FIRST_JSON_CONFIG_VERSION) {
                widgetPlacements.map { it.copy(nativeWidgetConfig = upgradeLegacyNativeWidgetConfig(it.nativeWidgetConfig)) }
            } else {
                widgetPlacements
            },
        )
    } else {
        this
    }
    return withClocks.copy(
        backupVersion = CURRENT_BACKUP_VERSION,
        facets = withClocks.facets.map { it.withOwnedHome(withClocks) },
    )
}

/**
 * What the facet showed in a backup from before it owned its Home: the launcher's dock, favorites, content mode and look wherever it
 * did not override them. A landscape value the facet left unset followed the launcher's own landscape value only while its portrait one was inherited.
 */
private fun BackupFacet.withOwnedHome(bundle: BackupBundle): BackupFacet {
    val s = bundle.settings
    fun resolved(value: String, launcherValue: String) = if (value == LEGACY_LAUNCHER_DEFAULT) launcherValue else value
    val ownDock = overrideDock == true
    val ownFavorites = overridingFavorites == true
    val ownContent = overrideApps == true
    return copy(
        dockApps = if (ownDock) dockApps else bundle.dockApps,
        dockFolderPlacements = if (ownDock) dockFolderPlacements else bundle.dockFolderPlacements,
        favorites = if (ownFavorites) favorites else bundle.defaultFavoriteApps,
        favoriteFolderPlacements = if (ownFavorites) favoriteFolderPlacements else bundle.defaultFavoriteFolderPlacements,
        listContentMode = if (ownContent) listContentMode else s.listContentMode,
        appsToShowCount = if (ownContent) appsToShowCount else s.appsToShowCount,
        dockDisplayMode = resolved(dockDisplayMode, s.dockDisplayMode),
        appRowPosition = resolved(appRowPosition, s.appRowPosition),
        appRowPresentation = resolved(appRowPresentation, s.appRowPresentation),
        appListVerticalAlignment = resolved(appListVerticalAlignment, s.appListVerticalAlignment),
        appListLayout = resolved(appListLayout, s.appListLayout),
        appListColumnAlignment = resolved(appListColumnAlignment, s.appListColumnAlignment),
        appListGridColumns = resolved(appListGridColumns, s.appListGridColumns),
        appListGridDisplayMode = resolved(appListGridDisplayMode, s.appListGridDisplayMode),
        appListShrinkToFit = resolved(appListShrinkToFit, s.appListShrinkToFit),
        landscapeAppRowPosition = landscapeAppRowPosition ?: s.landscapeAppRowPosition.takeIf { appRowPosition == LEGACY_LAUNCHER_DEFAULT },
        landscapeAppListVerticalAlignment = landscapeAppListVerticalAlignment
            ?: s.landscapeAppListVerticalAlignment.takeIf { appListVerticalAlignment == LEGACY_LAUNCHER_DEFAULT },
        landscapeAppListColumnAlignment = landscapeAppListColumnAlignment
            ?: s.landscapeAppListColumnAlignment.takeIf { appListColumnAlignment == LEGACY_LAUNCHER_DEFAULT },
        overrideApps = null,
        overridingFavorites = null,
        overrideDock = null,
    )
}

private fun BackupFacet.withUpgradedConfigs(settings: BackupSettings, upgradeWidgetConfigs: Boolean): BackupFacet {
    val upgraded = if (upgradeWidgetConfigs) {
        copy(
            clockWeatherLocation = upgradeLegacyNativeWidgetConfig(clockWeatherLocation),
            nativeStackPages = nativeStackPages.map { it.copy(nativeWidgetConfig = upgradeLegacyNativeWidgetConfig(it.nativeWidgetConfig)) },
        )
    } else {
        this
    }
    return upgraded.copy(clockConfig = NativeWidgetConfig(clock = upgraded.legacyClockConfig(settings)).encode())
}

/**
 * What the clock showed in a backup from before it had settings of its own: the facet's values while it overrode the clock or the
 * calendar, the launcher's otherwise. Calendar choices were never backed up for the launcher, so those clocks follow every calendar.
 */
private fun BackupFacet.legacyClockConfig(s: BackupSettings): ClockWidgetConfig {
    fun <T> pick(facetValue: T, launcherValue: T) = if (overrideClock) facetValue else launcherValue
    return ClockWidgetConfig(
        templateId = pick(clockTemplateId, s.clockTemplateId).toEnumOrDefault(ClockTemplateId.LIGHT_STACK),
        fontOption = pick(clockFontOption, s.clockFontOption).toEnumOrDefault(ClockFontOption.LAUNCHER_DEFAULT),
        colorOption = pick(clockColorOption, s.clockColorOption).toEnumOrDefault(ClockColorOption.THEME),
        accentColorOption = pick(clockAccentColorOption, s.clockAccentColorOption).toEnumOrDefault(ClockColorOption.ACCENT_PRIMARY),
        use24HourTime = pick(use24HourTime, s.use24HourTime),
        showMeridiem = pick(clockShowMeridiem, s.clockShowMeridiem),
        dateStyle = pick(clockDateStyle, s.clockDateStyle).toEnumOrDefault(ClockDateStyle.FULL),
        showAlarm = pick(clockShowAlarm, s.showNextAlarm),
        showBattery = pick(clockShowBattery, s.showBatteryPercentage),
        showWeather = pick(clockShowWeather, s.showWeather),
        weatherIconStyle = pick(clockWeatherIconStyle, s.weatherIconStyle).toEnumOrDefault(WeatherIconStyle.COLORED),
        weatherLocation = if (overrideClock) decodeWeatherLocation(clockWeatherLocation) else null,
        calendar = ClockCalendarConfig(
            showIndicator = if (overrideCalendar) showCalendarIndicator else s.showCalendarIndicator,
            itemsToShow = (if (overrideCalendar) calendarItemsToShow else s.calendarItemsToShow).coerceIn(CalendarDisplayOptions.MIN_ITEMS, CalendarDisplayOptions.MAX_ITEMS),
            showAllDay = if (overrideCalendar) showAllDayEvents else s.showAllDayEvents,
            calendarIds = if (overrideCalendar) selectedCalendarIds?.toSet() else null,
        ),
    )
}
