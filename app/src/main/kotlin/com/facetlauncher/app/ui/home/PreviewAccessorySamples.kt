package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.model.WeatherHour
import com.facetlauncher.app.domain.ClockAccessoryState
import com.facetlauncher.app.domain.RainSummary
import com.facetlauncher.app.domain.WeatherState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

private const val SAMPLE_ALARM_HOUR = 7
private const val SAMPLE_BATTERY_PERCENT = 64
private const val SAMPLE_TEMPERATURE_C = 21.0

/** Sample alarm and battery for a preview, so the accessory row shows where the live one sits (the facet's switches still apply). */
internal fun previewClockAccessories(): ClockAccessoryState {
    val alarm = LocalDate.now().plusDays(1).atTime(SAMPLE_ALARM_HOUR, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    return ClockAccessoryState(nextAlarmMillis = alarm, batteryPercent = SAMPLE_BATTERY_PERCENT, isCharging = false)
}

/** Sample weather for a preview: only the current hour's symbol and temperature are read by Home's accessory. */
internal fun previewWeather(): WeatherState = WeatherState(
    placeName = null,
    placeNameIsApproximate = false,
    now = WeatherHour(
        time = Instant.EPOCH,
        temperatureC = SAMPLE_TEMPERATURE_C,
        humidityPercent = null,
        pressureHpa = null,
        cloudPercent = null,
        windSpeedMps = null,
        windFromDegrees = null,
        precipitationMm = 0.0,
        symbolCode = "partlycloudy_day",
    ),
    hourly = emptyList(),
    days = emptyList(),
    summary = RainSummary.Dry,
    updatedAt = null,
    isOutdated = false,
    zone = ZoneOffset.UTC,
)
