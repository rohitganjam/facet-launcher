package com.facetlauncher.app.data.model

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

private const val CALENDAR_KEY = "cal"
private const val ALL_DAY_KEY = "allday"
private const val WEATHER_KEY = "wx"
private const val WEATHER_CURRENT = "current"
private const val WEATHER_CITY = "city"

/** The stored form: one line per field, tab-separated; `null` when the widget follows the launcher. */
fun NativeWidgetConfig.encode(): String? {
    val lines = buildList {
        calendarIds?.let { ids -> add((listOf(CALENDAR_KEY) + ids.sorted()).joinToString("\t")) }
        showAllDayEvents?.let { add("$ALL_DAY_KEY\t${if (it) 1 else 0}") }
        when (val location = weatherLocation) {
            null -> Unit
            WidgetWeatherLocation.Current -> add("$WEATHER_KEY\t$WEATHER_CURRENT")
            is WidgetWeatherLocation.City -> with(location.city) {
                add(listOf(WEATHER_KEY, WEATHER_CITY, name.replace('\t', ' ').replace('\n', ' '), latitude, longitude, timezone).joinToString("\t"))
            }
        }
    }
    return lines.takeIf { it.isNotEmpty() }?.joinToString("\n")
}

/** The inverse of [encode]; a line that does not hold a whole value is skipped, so a damaged value follows the launcher. */
fun decodeNativeWidgetConfig(encoded: String?): NativeWidgetConfig {
    var config = NativeWidgetConfig()
    encoded.orEmpty().lines().forEach { line ->
        val fields = line.split('\t')
        config = when (fields.firstOrNull()) {
            CALENDAR_KEY -> config.copy(calendarIds = fields.drop(1).toSet())
            ALL_DAY_KEY -> config.copy(showAllDayEvents = fields.getOrNull(1)?.let { it == "1" })
            WEATHER_KEY -> config.copy(weatherLocation = decodeWeatherLocation(fields.drop(1)) ?: config.weatherLocation)
            else -> config
        }
    }
    return config
}

private fun decodeWeatherLocation(fields: List<String>): WidgetWeatherLocation? = when (fields.firstOrNull()) {
    WEATHER_CURRENT -> WidgetWeatherLocation.Current
    WEATHER_CITY -> fields.getOrNull(1)?.let { name ->
        weatherCityOrNull(name, fields.getOrNull(2)?.toDoubleOrNull(), fields.getOrNull(3)?.toDoubleOrNull(), fields.getOrNull(4))
    }?.let(WidgetWeatherLocation::City)
    else -> null
}
