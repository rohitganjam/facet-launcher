package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.ClockCalendarConfig
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.effectiveCalendarDisplay

/**
 * What [facet]'s clock looks like and shows today, as the config a clock widget carries: the facet's own values while it overrides the
 * clock or the calendar, the launcher's otherwise. This is what moving each facet to per-clock settings keeps, so nothing changes on screen.
 */
fun effectiveClockConfig(facet: Facet?, settings: LauncherSettings): ClockWidgetConfig {
    val style = resolveFacetStyle(facet, settings).clock
    val display = facet.effectiveCalendarDisplay(settings)
    val ownCalendar = facet?.takeIf { it.overrideCalendar }
    return ClockWidgetConfig(
        templateId = style.templateId,
        fontOption = style.fontOption,
        colorOption = style.colorOption,
        accentColorOption = style.accentColorOption,
        use24HourTime = style.use24HourTime,
        showMeridiem = style.showMeridiem,
        dateStyle = style.dateStyle,
        showAlarm = style.showAlarm,
        showBattery = style.showBattery,
        showWeather = style.showWeather,
        weatherIconStyle = style.weatherIconStyle,
        weatherLocation = style.weatherLocation,
        calendar = ClockCalendarConfig(
            showIndicator = display.showIndicator,
            itemsToShow = display.itemsToShow,
            showAllDay = ownCalendar?.showAllDayEvents ?: settings.showAllDayEvents,
            calendarIds = if (ownCalendar != null) ownCalendar.selectedCalendarIds else settings.selectedCalendarIds,
        ),
    )
}
