package com.facetlauncher.app.ui.home.weather

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalConfiguration
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.PressureUnit
import com.facetlauncher.app.data.model.RainUnit
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.WindSpeedUnit
import com.facetlauncher.app.domain.WeatherUnits
import java.util.Locale
import kotlin.math.roundToInt

private const val WHOLE_AMOUNT_FROM = 10.0

/** "27°C" or "81°F" in a resolved [unit]. */
@Composable
internal fun temperatureText(celsius: Double, unit: TemperatureUnit): String =
    stringResource(
        R.string.weather_temperature_format,
        WeatherUnits.temperature(celsius, unit).roundToInt(),
        stringResource(temperatureUnitLetterRes(unit)),
    )

@StringRes
internal fun temperatureUnitLetterRes(unit: TemperatureUnit): Int =
    if (unit == TemperatureUnit.FAHRENHEIT) R.string.weather_temperature_unit_fahrenheit else R.string.weather_temperature_unit_celsius

@StringRes
internal fun windUnitRes(unit: WindSpeedUnit): Int = when (unit) {
    WindSpeedUnit.MPH -> R.string.weather_unit_short_mph
    WindSpeedUnit.MS -> R.string.weather_unit_short_ms
    WindSpeedUnit.KMH, WindSpeedUnit.AUTO -> R.string.weather_unit_short_kmh
}

@StringRes
internal fun rainUnitRes(unit: RainUnit): Int = if (unit == RainUnit.INCH) R.string.weather_unit_short_inch else R.string.weather_unit_short_mm

@StringRes
internal fun pressureUnitRes(unit: PressureUnit): Int = when (unit) {
    PressureUnit.INHG -> R.string.weather_unit_short_inhg
    PressureUnit.MMHG -> R.string.weather_unit_short_mmhg
    PressureUnit.HPA, PressureUnit.AUTO -> R.string.weather_unit_short_hpa
}

/** The wind speed as a bare number in [unit] (10 for 2.9 m/s in km/h), for the hourly strip where the unit is in the legend. */
internal fun windNumber(metresPerSecond: Double, unit: WindSpeedUnit): String =
    WeatherUnits.windSpeed(metresPerSecond, unit).roundToInt().toString()

/** The rain in the hour as a bare number in [unit]: one decimal under 10 (0.4, 2.8) and whole above; inches get two under 1. */
internal fun rainNumber(millimetres: Double, unit: RainUnit): String {
    val value = WeatherUnits.rain(millimetres, unit)
    return when {
        unit == RainUnit.INCH && value < 1.0 -> String.format(Locale.getDefault(), "%.2f", value)
        value < WHOLE_AMOUNT_FROM -> String.format(Locale.getDefault(), "%.1f", value)
        else -> value.roundToInt().toString()
    }
}

/** "63 mm" or "2.5 in" for a total. */
@Composable
internal fun rainWithUnit(millimetres: Double, unit: RainUnit): String =
    stringResource(R.string.weather_value_with_unit, rainNumber(millimetres, unit), stringResource(rainUnitRes(unit)))

/** "10 km/h" or, with a direction, "10 km/h E". */
@Composable
internal fun windWithUnit(metresPerSecond: Double, unit: WindSpeedUnit, fromDegrees: Double? = null): String {
    val speed = stringResource(R.string.weather_value_with_unit, windNumber(metresPerSecond, unit), stringResource(windUnitRes(unit)))
    return if (fromDegrees == null) speed else stringResource(R.string.weather_wind_with_direction, speed, compassPoint(fromDegrees))
}

@Composable
internal fun pressureWithUnit(hectopascals: Double, unit: PressureUnit): String {
    val value = WeatherUnits.pressure(hectopascals, unit)
    val locale = LocalConfiguration.current.locales[0]
    val number = if (unit == PressureUnit.INHG) String.format(locale, "%.2f", value) else value.roundToInt().toString()
    return stringResource(R.string.weather_value_with_unit, number, stringResource(pressureUnitRes(unit)))
}
