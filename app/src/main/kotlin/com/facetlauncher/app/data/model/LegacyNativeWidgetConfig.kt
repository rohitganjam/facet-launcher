package com.facetlauncher.app.data.model

/*
 * The form a widget's setting was stored in up to schema 41 and backup version 3: one line per field, tab-separated. Only the
 * schema 41 to 42 migration and the importer of an older backup read it, to rewrite it as the current JSON; delete this file with
 * the support for those backups.
 */

private const val CALENDAR_KEY = "cal"
private const val ALL_DAY_KEY = "allday"
private const val WEATHER_KEY = "wx"
private const val WEATHER_CURRENT = "current"
private const val WEATHER_CITY = "city"

/** [encoded] in the tab-separated form, as the current JSON form (`null` when it follows the launcher). */
fun upgradeLegacyNativeWidgetConfig(encoded: String?): String? = decodeLegacyNativeWidgetConfig(encoded).encode()

private fun decodeLegacyNativeWidgetConfig(encoded: String?): NativeWidgetConfig {
    var config = NativeWidgetConfig()
    encoded.orEmpty().lines().forEach { line ->
        val fields = line.split('\t')
        config = when (fields.firstOrNull()) {
            CALENDAR_KEY -> config.copy(calendarIds = fields.drop(1).toSet())
            ALL_DAY_KEY -> config.copy(showAllDayEvents = fields.getOrNull(1)?.let { it == "1" })
            WEATHER_KEY -> config.copy(weatherLocation = decodeLegacyWeatherLocation(fields.drop(1)) ?: config.weatherLocation)
            else -> config
        }
    }
    return config
}

private fun decodeLegacyWeatherLocation(fields: List<String>): WidgetWeatherLocation? = when (fields.firstOrNull()) {
    WEATHER_CURRENT -> WidgetWeatherLocation.Current
    WEATHER_CITY -> fields.getOrNull(1)?.let { name ->
        weatherCityOrNull(name, fields.getOrNull(2)?.toDoubleOrNull(), fields.getOrNull(3)?.toDoubleOrNull(), fields.getOrNull(4))
    }?.let(WidgetWeatherLocation::City)
    else -> null
}
