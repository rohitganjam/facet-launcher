package com.facetlauncher.app.data.model

/** Each unit option has an `AUTO` that follows the device locale; see `WeatherUnits`. */
enum class TemperatureUnit { AUTO, CELSIUS, FAHRENHEIT }

enum class WindSpeedUnit { AUTO, KMH, MPH, MS }

enum class RainUnit { AUTO, MM, INCH }

enum class PressureUnit { AUTO, HPA, INHG, MMHG }

/** How weather pictures are drawn: in the surrounding text colour, or in their own colours. Launcher-wide. */
enum class WeatherIconStyle { MONOCHROME, COLORED }

/** Where the weather is for: the phone's own position, or one chosen city. Launcher-wide, never per facet. */
enum class WeatherLocationMode { CURRENT, CITY }

/** The city picked under [WeatherLocationMode.CITY]. [timezone] is an IANA id such as `Asia/Kolkata`. */
data class WeatherCity(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
)

/** How many cities the location screen keeps for quick switching. */
const val MAX_SAVED_WEATHER_CITIES = 10

/** Same place: a city is identified by its name and rounded position, not by its zone. */
fun WeatherCity.isSamePlaceAs(other: WeatherCity): Boolean =
    name == other.name && latitude == other.latitude && longitude == other.longitude

/** The saved cities as one stored string, a city per line with tab-separated fields. */
fun encodeWeatherCities(cities: List<WeatherCity>): String =
    cities.joinToString("\n") { listOf(it.name.clean(), it.latitude, it.longitude, it.timezone.clean()).joinToString("\t") }

/** The inverse of [encodeWeatherCities]; a line that does not hold a whole city is skipped. */
fun decodeWeatherCities(encoded: String?): List<WeatherCity> =
    encoded.orEmpty().lines().mapNotNull { line ->
        val fields = line.split('\t')
        if (fields.size == WEATHER_CITY_FIELDS) weatherCityOrNull(fields[0], fields[1].toDoubleOrNull(), fields[2].toDoubleOrNull(), fields[3]) else null
    }

private const val WEATHER_CITY_FIELDS = 4

private fun String.clean() = replace('\t', ' ').replace('\n', ' ')

/** A city from its four stored fields, or `null` unless all four are present (a half-saved city is no city). */
fun weatherCityOrNull(name: String?, latitude: Double?, longitude: Double?, timezone: String?): WeatherCity? =
    if (name != null && timezone != null) latitude?.let { lat -> longitude?.let { lon -> WeatherCity(name, lat, lon, timezone) } } else null
