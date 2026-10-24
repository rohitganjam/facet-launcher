package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.WeatherHour
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.abs

/** One local calendar day of the forecast. Past the first days the feed is 6-hourly, so those highs and lows are approximate. */
data class WeatherDay(
    val date: LocalDate,
    val lowC: Double,
    val highC: Double,
    /** The symbol of the point nearest local noon that has one. */
    val symbolCode: String?,
    /** The rain in the day's hourly points, or `null` when it has none (a 6-hourly day). */
    val precipitationMm: Double?,
)

private val NOON = LocalTime.NOON

/**
 * Groups [hours] into local calendar days in [zone]. Today's low and high include the hours stored earlier in the
 * day, which is why the repository keeps them; a day's boundaries follow [zone]'s own rules, so a DST day has 23 or 25 hours.
 */
fun dailySummaries(hours: List<WeatherHour>, zone: ZoneId): List<WeatherDay> =
    hours.groupBy { it.time.atZone(zone).toLocalDate() }
        .toSortedMap()
        .map { (date, points) ->
            val rain = points.mapNotNull { it.precipitationMm }.takeIf { it.isNotEmpty() }?.sum()
            WeatherDay(
                date = date,
                lowC = points.minOf { it.temperatureC },
                highC = points.maxOf { it.temperatureC },
                symbolCode = points.filter { it.symbolCode != null }.minByOrNull { distanceFromNoon(it, zone) }?.symbolCode,
                precipitationMm = rain,
            )
        }

private fun distanceFromNoon(hour: WeatherHour, zone: ZoneId): Long {
    val local = hour.time.atZone(zone).toLocalTime()
    return abs(Duration.between(NOON, local).toMinutes())
}
