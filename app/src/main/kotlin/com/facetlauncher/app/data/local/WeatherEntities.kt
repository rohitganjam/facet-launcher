package com.facetlauncher.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One stored forecast hour, keyed by its UTC instant. See `WeatherHour`. */
@Entity(tableName = "weather_hours")
data class WeatherHourEntity(
    @PrimaryKey val timeEpochSecond: Long,
    val temperatureC: Double,
    val humidityPercent: Double?,
    val pressureHpa: Double?,
    val cloudPercent: Double?,
    val windSpeedMps: Double?,
    val windFromDegrees: Double?,
    val precipitationMm: Double?,
    val symbolCode: String?,
)

/** The single row (`id` 1) of fetch state: which place the hours are for, `Expires`, the last attempt and any rate-limit block. */
@Entity(tableName = "weather_meta")
data class WeatherMetaEntity(
    @PrimaryKey val id: Int = 1,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitudeM: Int? = null,
    val fetchedAtEpochSecond: Long? = null,
    val expiresAtEpochSecond: Long? = null,
    val lastModified: String? = null,
    val lastAttemptAtEpochSecond: Long? = null,
    val blockedUntilEpochSecond: Long? = null,
    /** The name resolved for this place, and which source gave it (`GEOCODER` or `OFFLINE`). */
    val placeName: String? = null,
    val placeNameSource: String? = null,
)
