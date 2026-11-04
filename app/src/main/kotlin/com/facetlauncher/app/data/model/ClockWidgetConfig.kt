package com.facetlauncher.app.data.model

/**
 * Everything one clock widget is set to: how it looks, what it shows beside the time, and the events under it. A clock has no
 * launcher-wide default and no override; each one carries all of it.
 */
data class ClockWidgetConfig(
    val templateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    val fontOption: ClockFontOption = ClockFontOption.LAUNCHER_DEFAULT,
    val colorOption: ClockColorOption = ClockColorOption.THEME,
    val accentColorOption: ClockColorOption = ClockColorOption.ACCENT_PRIMARY,
    val use24HourTime: Boolean = false,
    val showMeridiem: Boolean = false,
    val dateStyle: ClockDateStyle = ClockDateStyle.FULL,
    val showAlarm: Boolean = true,
    val showBattery: Boolean = true,
    val showWeather: Boolean = false,
    val weatherIconStyle: WeatherIconStyle = WeatherIconStyle.COLORED,
    /** Where this clock shows the weather for; `null` follows Weather settings. */
    val weatherLocation: WidgetWeatherLocation? = null,
    val calendar: ClockCalendarConfig = ClockCalendarConfig(),
)

/** The events under a clock: [calendarIds] `null` is "all calendars" (see [selectsCalendar]), so a calendar added later shows too. */
data class ClockCalendarConfig(
    val showIndicator: Boolean = true,
    val itemsToShow: Int = CalendarDisplayOptions.MAX_ITEMS,
    val showAllDay: Boolean = true,
    val calendarIds: Set<String>? = null,
)
