package com.facetlauncher.app.data.model

/** Each unit option has an `AUTO` that follows the device locale; see `WeatherUnits`. */
enum class TemperatureUnit { AUTO, CELSIUS, FAHRENHEIT }

enum class WindSpeedUnit { AUTO, KMH, MPH, MS }

enum class RainUnit { AUTO, MM, INCH }

enum class PressureUnit { AUTO, HPA, INHG, MMHG }

/** Where the weather is for: the phone's own position, or one chosen city. Launcher-wide, never per facet. */
enum class WeatherLocationMode { CURRENT, CITY }

/** The city picked under [WeatherLocationMode.CITY]. [timezone] is an IANA id such as `Asia/Kolkata`. */
data class WeatherCity(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
)

/** A city from its four stored fields, or `null` unless all four are present (a half-saved city is no city). */
fun weatherCityOrNull(name: String?, latitude: Double?, longitude: Double?, timezone: String?): WeatherCity? =
    if (name != null && timezone != null) latitude?.let { lat -> longitude?.let { lon -> WeatherCity(name, lat, lon, timezone) } } else null
