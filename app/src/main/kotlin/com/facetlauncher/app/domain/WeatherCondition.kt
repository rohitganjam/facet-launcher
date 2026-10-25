package com.facetlauncher.app.domain

/** What the sky is doing, in words the app owns (MET's symbol codes are for icons and are too fine-grained for text). */
enum class WeatherCondition {
    CLEAR, MOSTLY_CLEAR, PARTLY_CLOUDY, CLOUDY, FOG,
    LIGHT_RAIN, RAIN, HEAVY_RAIN, SLEET, SNOW, THUNDERSTORM,
}

private const val WET_MM = 0.2
private const val HEAVY_MM = 7.6
private const val MODERATE_MM = 2.5

/**
 * The condition for a MET symbol code such as `partlycloudy_day` or `lightrainshowers_night`, or `null` for a code this
 * app does not know. For a rain symbol the hour's own amount, when it has one of at least 0.2 mm, sets how hard it
 * rains (light under 2.5 mm, heavy from 7.6 mm), because MET's own rain names are tuned for Norway: its "heavy rain"
 * starts at about 1 mm in an hour.
 */
fun conditionOf(symbolCode: String?, precipitationMm: Double? = null): WeatherCondition? {
    val name = symbolCode?.substringBefore('_')?.lowercase().orEmpty()
    return when {
        name.isEmpty() -> null
        "thunder" in name -> WeatherCondition.THUNDERSTORM
        "snow" in name -> WeatherCondition.SNOW
        "sleet" in name -> WeatherCondition.SLEET
        "rain" in name -> rainCondition(name, precipitationMm)
        name == "fog" -> WeatherCondition.FOG
        name == "clearsky" -> WeatherCondition.CLEAR
        name == "fair" -> WeatherCondition.MOSTLY_CLEAR
        name == "partlycloudy" -> WeatherCondition.PARTLY_CLOUDY
        name == "cloudy" -> WeatherCondition.CLOUDY
        else -> null
    }
}

private fun rainCondition(name: String, precipitationMm: Double?): WeatherCondition = when {
    precipitationMm != null && precipitationMm >= WET_MM -> when {
        precipitationMm >= HEAVY_MM -> WeatherCondition.HEAVY_RAIN
        precipitationMm >= MODERATE_MM -> WeatherCondition.RAIN
        else -> WeatherCondition.LIGHT_RAIN
    }
    name.startsWith("light") -> WeatherCondition.LIGHT_RAIN
    name.startsWith("heavy") -> WeatherCondition.HEAVY_RAIN
    else -> WeatherCondition.RAIN
}

/** MET marks a night symbol with `_night`; every other suffix, and none, is drawn as day. */
fun isNightSymbol(symbolCode: String?): Boolean = symbolCode?.endsWith("_night") == true
