package com.facetlauncher.app.data.model

import java.time.Instant

/**
 * One forecast point. The feed is hourly for the first couple of days and 6-hourly after that;
 * [precipitationMm] is the amount in the hour starting at [time] (`null` on a 6-hourly point).
 */
data class WeatherHour(
    val time: Instant,
    val temperatureC: Double,
    val humidityPercent: Double?,
    val pressureHpa: Double?,
    val cloudPercent: Double?,
    val windSpeedMps: Double?,
    val windFromDegrees: Double?,
    val precipitationMm: Double?,
    /** MET's symbol, e.g. `partlycloudy_day`: the next hour's, or the next six hours' on a 6-hourly point. */
    val symbolCode: String?,
)

data class WeatherForecast(val updatedAt: Instant?, val hours: List<WeatherHour>)

/** Where a forecast is for: coordinates rounded to [COORDINATE_DECIMALS], so one place never differs by float noise. */
data class WeatherPlace(val latitude: Double, val longitude: Double, val altitudeM: Int? = null) {
    companion object {
        const val COORDINATE_DECIMALS = 2

        fun rounded(latitude: Double, longitude: Double, altitudeM: Int? = null): WeatherPlace {
            val scale = Math.pow(10.0, COORDINATE_DECIMALS.toDouble())
            return WeatherPlace(Math.round(latitude * scale) / scale, Math.round(longitude * scale) / scale, altitudeM)
        }
    }
}
