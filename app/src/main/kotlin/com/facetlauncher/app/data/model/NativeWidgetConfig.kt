package com.facetlauncher.app.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Where one Weather widget reads its forecast for, when it does not follow the launcher-wide location. */
sealed interface WidgetWeatherLocation {
    data object Current : WidgetWeatherLocation
    data class City(val city: WeatherCity) : WidgetWeatherLocation
}

/**
 * What one widget Facet draws itself is set to, overriding the launcher-wide setting. A `null` field follows the
 * launcher: [calendarIds] and [showAllDayEvents] for Agenda (an empty set is "no calendars", not "follow"), [weatherLocation] for Weather.
 */
data class NativeWidgetConfig(
    val calendarIds: Set<String>? = null,
    /** Agenda: whether all-day events show; `null` follows the launcher. */
    val showAllDayEvents: Boolean? = null,
    val weatherLocation: WidgetWeatherLocation? = null,
    /** A clock's own settings; a clock has no launcher-wide setting to follow, so a clock widget always carries them. */
    val clock: ClockWidgetConfig? = null,
) {
    val followsLauncher: Boolean get() = calendarIds == null && showAllDayEvents == null && weatherLocation == null && clock == null
}

/** The stored shape of [NativeWidgetConfig]: JSON, with a field left out while it follows the launcher. */
@Serializable
private data class StoredConfig(
    val calendarIds: List<String>? = null,
    val showAllDayEvents: Boolean? = null,
    val weather: StoredWeather? = null,
    val clock: StoredClock? = null,
)

/** A clock's settings, every enum by name; a name this version does not know reads as that field's default. */
@Serializable
private data class StoredClock(
    val template: String? = null,
    val font: String? = null,
    val color: String? = null,
    val accentColor: String? = null,
    val use24Hour: Boolean = false,
    val meridiem: Boolean = false,
    val dateStyle: String? = null,
    val alarm: Boolean = true,
    val battery: Boolean = true,
    val weather: Boolean = false,
    val weatherIcons: String? = null,
    val weatherLocation: StoredWeather? = null,
    val calendarIndicator: Boolean = true,
    val calendarItems: Int = CalendarDisplayOptions.MAX_ITEMS,
    val calendarAllDay: Boolean = true,
    val calendarIds: List<String>? = null,
)

/** A weather location: [city] unset means the phone's position, otherwise the chosen city. */
@Serializable
private data class StoredWeather(
    val city: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timezone: String? = null,
)

private val configJson = Json { ignoreUnknownKeys = true }

/** The stored form: JSON, or `null` when the widget follows the launcher. */
fun NativeWidgetConfig.encode(): String? {
    if (followsLauncher) return null
    return configJson.encodeToString(StoredConfig(calendarIds?.sorted(), showAllDayEvents, weatherLocation?.toStored(), clock?.toStored()))
}

private fun WidgetWeatherLocation.toStored(): StoredWeather = when (this) {
    WidgetWeatherLocation.Current -> StoredWeather()
    is WidgetWeatherLocation.City -> with(city) { StoredWeather(name, latitude, longitude, timezone) }
}

private fun ClockWidgetConfig.toStored() = StoredClock(
    template = templateId.name, font = fontOption.name, color = colorOption.name, accentColor = accentColorOption.name,
    use24Hour = use24HourTime, meridiem = showMeridiem, dateStyle = dateStyle.name, alarm = showAlarm, battery = showBattery,
    weather = showWeather, weatherIcons = weatherIconStyle.name, weatherLocation = weatherLocation?.toStored(),
    calendarIndicator = calendar.showIndicator, calendarItems = calendar.itemsToShow, calendarAllDay = calendar.showAllDay,
    calendarIds = calendar.calendarIds?.sorted(),
)

private inline fun <reified E : Enum<E>> String?.enumOr(default: E): E = this?.let { name -> enumValues<E>().firstOrNull { it.name == name } } ?: default

private fun StoredClock.toConfig(): ClockWidgetConfig {
    val defaults = ClockWidgetConfig()
    return ClockWidgetConfig(
        templateId = template.enumOr(defaults.templateId), fontOption = font.enumOr(defaults.fontOption),
        colorOption = color.enumOr(defaults.colorOption), accentColorOption = accentColor.enumOr(defaults.accentColorOption),
        use24HourTime = use24Hour, showMeridiem = meridiem, dateStyle = dateStyle.enumOr(defaults.dateStyle),
        showAlarm = alarm, showBattery = battery, showWeather = weather, weatherIconStyle = weatherIcons.enumOr(defaults.weatherIconStyle),
        weatherLocation = weatherLocation?.toLocation(),
        calendar = ClockCalendarConfig(calendarIndicator, calendarItems, calendarAllDay, calendarIds?.toSet()),
    )
}

/** The inverse of [encode]; a value that does not parse follows the launcher. */
fun decodeNativeWidgetConfig(encoded: String?): NativeWidgetConfig {
    if (encoded.isNullOrBlank()) return NativeWidgetConfig()
    val stored = runCatching { configJson.decodeFromString<StoredConfig>(encoded) }.getOrNull() ?: return NativeWidgetConfig()
    return NativeWidgetConfig(
        calendarIds = stored.calendarIds?.toSet(),
        showAllDayEvents = stored.showAllDayEvents,
        weatherLocation = stored.weather?.toLocation(),
        clock = stored.clock?.toConfig(),
    )
}

private fun StoredWeather.toLocation(): WidgetWeatherLocation? =
    if (city == null) WidgetWeatherLocation.Current else weatherCityOrNull(city, latitude, longitude, timezone)?.let(WidgetWeatherLocation::City)

/** The stored form of a weather location on its own (a facet's clock): the widget codec, so one format for both; `null` follows Weather settings. */
fun encodeWeatherLocation(location: WidgetWeatherLocation?): String? = NativeWidgetConfig(weatherLocation = location).encode()

/** The inverse of [encodeWeatherLocation]; a damaged value follows Weather settings. */
fun decodeWeatherLocation(encoded: String?): WidgetWeatherLocation? = decodeNativeWidgetConfig(encoded).weatherLocation
