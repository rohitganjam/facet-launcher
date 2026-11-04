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
) {
    val followsLauncher: Boolean get() = calendarIds == null && showAllDayEvents == null && weatherLocation == null
}

/** The stored shape of [NativeWidgetConfig]: JSON, with a field left out while it follows the launcher. */
@Serializable
private data class StoredConfig(
    val calendarIds: List<String>? = null,
    val showAllDayEvents: Boolean? = null,
    val weather: StoredWeather? = null,
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
    val weather = when (val location = weatherLocation) {
        null -> null
        WidgetWeatherLocation.Current -> StoredWeather()
        is WidgetWeatherLocation.City -> with(location.city) { StoredWeather(name, latitude, longitude, timezone) }
    }
    return configJson.encodeToString(StoredConfig(calendarIds?.sorted(), showAllDayEvents, weather))
}

/** The inverse of [encode]; a value that does not parse follows the launcher. */
fun decodeNativeWidgetConfig(encoded: String?): NativeWidgetConfig {
    if (encoded.isNullOrBlank()) return NativeWidgetConfig()
    val stored = runCatching { configJson.decodeFromString<StoredConfig>(encoded) }.getOrNull() ?: return NativeWidgetConfig()
    return NativeWidgetConfig(
        calendarIds = stored.calendarIds?.toSet(),
        showAllDayEvents = stored.showAllDayEvents,
        weatherLocation = stored.weather?.toLocation(),
    )
}

private fun StoredWeather.toLocation(): WidgetWeatherLocation? =
    if (city == null) WidgetWeatherLocation.Current else weatherCityOrNull(city, latitude, longitude, timezone)?.let(WidgetWeatherLocation::City)

/** The stored form of a weather location on its own (a facet's clock): the widget codec, so one format for both; `null` follows Weather settings. */
fun encodeWeatherLocation(location: WidgetWeatherLocation?): String? = NativeWidgetConfig(weatherLocation = location).encode()

/** The inverse of [encodeWeatherLocation]; a damaged value follows Weather settings. */
fun decodeWeatherLocation(encoded: String?): WidgetWeatherLocation? = decodeNativeWidgetConfig(encoded).weatherLocation
