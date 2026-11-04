package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.ClockCalendarConfig
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.clockWidgetConfig
import com.facetlauncher.app.data.model.resolveOverride

/**
 * The clock's settings in force for [facet]: its clock page's own config, or, until [MaterializeClockConfigsUseCase] has written one,
 * what the clock shows today ([legacyClockConfig]). `null` facet (none yet) is the launcher's.
 */
fun effectiveClockConfig(facet: Facet?, settings: LauncherSettings): ClockWidgetConfig =
    facet?.clockWidgetConfig() ?: legacyClockConfig(facet, settings)

/**
 * What [facet]'s clock looks like and shows from the launcher-wide settings and the facet's override switches: the facet's own values while
 * it overrides the clock or the calendar, the launcher's otherwise. This is what a clock without a config of its own shows, and what the
 * migration to per-clock settings writes, so nothing changes on screen.
 */
fun legacyClockConfig(facet: Facet?, settings: LauncherSettings): ClockWidgetConfig {
    val ownCalendar = facet?.takeIf { it.overrideCalendar }
    return ClockWidgetConfig(
        templateId = facet.resolveOverride({ it.overrideClock }, { it.clockTemplateId }, settings.clockTemplateId),
        fontOption = facet.resolveOverride({ it.overrideClock }, { it.clockFontOption }, settings.clockFontOption),
        colorOption = facet.resolveOverride({ it.overrideClock }, { it.clockColorOption }, settings.clockColorOption),
        accentColorOption = facet.resolveOverride({ it.overrideClock }, { it.clockAccentColorOption }, settings.clockAccentColorOption),
        use24HourTime = facet.resolveOverride({ it.overrideClock }, { it.use24HourTime }, settings.use24HourTime),
        showMeridiem = facet.resolveOverride({ it.overrideClock }, { it.clockShowMeridiem }, settings.clockShowMeridiem),
        dateStyle = facet.resolveOverride({ it.overrideClock }, { it.clockDateStyle }, settings.clockDateStyle),
        showAlarm = facet.resolveOverride({ it.overrideClock }, { it.clockShowAlarm }, settings.showNextAlarm),
        showBattery = facet.resolveOverride({ it.overrideClock }, { it.clockShowBattery }, settings.showBatteryPercentage),
        showWeather = facet.resolveOverride({ it.overrideClock }, { it.clockShowWeather }, settings.showWeather),
        weatherIconStyle = facet.resolveOverride({ it.overrideClock }, { it.clockWeatherIconStyle }, settings.weatherIconStyle),
        weatherLocation = facet.resolveOverride({ it.overrideClock }, { it.clockWeatherLocation }, null),
        calendar = ClockCalendarConfig(
            showIndicator = ownCalendar?.showCalendarIndicator ?: settings.showCalendarIndicator,
            itemsToShow = ownCalendar?.calendarItemsToShow ?: settings.calendarItemsToShow,
            showAllDay = ownCalendar?.showAllDayEvents ?: settings.showAllDayEvents,
            calendarIds = if (ownCalendar != null) ownCalendar.selectedCalendarIds else settings.selectedCalendarIds,
        ),
    )
}
