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
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.data.model.decodeWeatherLocation
import com.facetlauncher.app.data.model.encode
import com.facetlauncher.app.data.model.upgradeLegacyNativeWidgetConfig

/** Backup version that first wrote widget settings as JSON; older ones wrote tab-separated lines. */
private const val FIRST_JSON_CONFIG_VERSION = 4

/** Backup version whose facets carry the clock's own settings; older ones kept them on the facet and the launcher. */
private const val FIRST_CLOCK_CONFIG_VERSION = 5

/** [this] in the current shape, so the import below this point only ever sees one: older backups get their widget and clock settings rewritten. */
fun BackupBundle.upgradedToCurrent(): BackupBundle {
    if (backupVersion >= FIRST_CLOCK_CONFIG_VERSION) return this
    return copy(
        backupVersion = CURRENT_BACKUP_VERSION,
        facets = facets.map { it.withUpgradedConfigs(settings, upgradeWidgetConfigs = backupVersion < FIRST_JSON_CONFIG_VERSION) },
        widgetPlacements = if (backupVersion < FIRST_JSON_CONFIG_VERSION) {
            widgetPlacements.map { it.copy(nativeWidgetConfig = upgradeLegacyNativeWidgetConfig(it.nativeWidgetConfig)) }
        } else {
            widgetPlacements
        },
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
