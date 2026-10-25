package com.facetlauncher.app.data.weather

import com.facetlauncher.app.data.model.WeatherForecast
import com.facetlauncher.app.data.model.WeatherHour
import java.time.Instant
import java.time.format.DateTimeParseException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

@Serializable
private data class MetResponse(val properties: MetProperties)

@Serializable
private data class MetProperties(val meta: MetMeta? = null, val timeseries: List<MetEntry> = emptyList())

@Serializable
private data class MetMeta(@SerialName("updated_at") val updatedAt: String? = null)

@Serializable
private data class MetEntry(val time: String, val data: MetData)

@Serializable
private data class MetData(
    val instant: MetInstant,
    @SerialName("next_1_hours") val next1Hours: MetPeriod? = null,
    @SerialName("next_6_hours") val next6Hours: MetPeriod? = null,
)

@Serializable
private data class MetInstant(val details: MetDetails = MetDetails())

@Serializable
private data class MetDetails(
    @SerialName("air_temperature") val airTemperature: Double? = null,
    @SerialName("relative_humidity") val relativeHumidity: Double? = null,
    @SerialName("air_pressure_at_sea_level") val airPressureAtSeaLevel: Double? = null,
    @SerialName("cloud_area_fraction") val cloudAreaFraction: Double? = null,
    @SerialName("wind_speed") val windSpeed: Double? = null,
    @SerialName("wind_from_direction") val windFromDirection: Double? = null,
)

@Serializable
private data class MetPeriod(val summary: MetSummary? = null, val details: MetPeriodDetails? = null)

@Serializable
private data class MetSummary(@SerialName("symbol_code") val symbolCode: String? = null)

@Serializable
private data class MetPeriodDetails(@SerialName("precipitation_amount") val precipitationAmount: Double? = null)

/**
 * Parses a Locationforecast 2.0 `compact` response. Every `next_*` block is optional (the 6-hourly points have no
 * `next_1_hours`, and the last points have none at all). A point with no temperature or an unreadable time is
 * skipped; `null` means the text is not a forecast at all.
 */
internal fun parseMetForecast(text: String): WeatherForecast? {
    val response = decode(text) ?: return null
    val hours = response.properties.timeseries.mapNotNull { entry ->
        val time = parseInstant(entry.time) ?: return@mapNotNull null
        val details = entry.data.instant.details
        val temperature = details.airTemperature ?: return@mapNotNull null
        val nextHour = entry.data.next1Hours
        WeatherHour(
            time = time,
            temperatureC = temperature,
            humidityPercent = details.relativeHumidity,
            pressureHpa = details.airPressureAtSeaLevel,
            cloudPercent = details.cloudAreaFraction,
            windSpeedMps = details.windSpeed,
            windFromDegrees = details.windFromDirection,
            precipitationMm = nextHour?.details?.precipitationAmount,
            symbolCode = nextHour?.summary?.symbolCode ?: entry.data.next6Hours?.summary?.symbolCode,
        )
    }.sortedBy { it.time }
    return WeatherForecast(updatedAt = response.properties.meta?.updatedAt?.let(::parseInstant), hours = hours)
}

private fun decode(text: String): MetResponse? = try {
    json.decodeFromString<MetResponse>(text)
} catch (_: SerializationException) {
    null
} catch (_: IllegalArgumentException) {
    null
}

private fun parseInstant(text: String): Instant? = try {
    Instant.parse(text)
} catch (_: DateTimeParseException) {
    null
}
