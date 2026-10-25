package com.facetlauncher.app.ui.settings

import androidx.annotation.StringRes
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.PressureUnit
import com.facetlauncher.app.data.model.RainUnit
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.data.model.WindSpeedUnit

@StringRes
internal fun temperatureUnitLabel(unit: TemperatureUnit): Int = when (unit) {
    TemperatureUnit.AUTO -> R.string.weather_unit_auto
    TemperatureUnit.CELSIUS -> R.string.weather_unit_celsius
    TemperatureUnit.FAHRENHEIT -> R.string.weather_unit_fahrenheit
}

@StringRes
internal fun windSpeedUnitLabel(unit: WindSpeedUnit): Int = when (unit) {
    WindSpeedUnit.AUTO -> R.string.weather_unit_auto
    WindSpeedUnit.KMH -> R.string.weather_unit_option_kmh
    WindSpeedUnit.MPH -> R.string.weather_unit_option_mph
    WindSpeedUnit.MS -> R.string.weather_unit_option_ms
}

@StringRes
internal fun rainUnitLabel(unit: RainUnit): Int = when (unit) {
    RainUnit.AUTO -> R.string.weather_unit_auto
    RainUnit.MM -> R.string.weather_unit_option_mm
    RainUnit.INCH -> R.string.weather_unit_option_inch
}

@StringRes
internal fun pressureUnitLabel(unit: PressureUnit): Int = when (unit) {
    PressureUnit.AUTO -> R.string.weather_unit_auto
    PressureUnit.HPA -> R.string.weather_unit_option_hpa
    PressureUnit.INHG -> R.string.weather_unit_option_inhg
    PressureUnit.MMHG -> R.string.weather_unit_option_mmhg
}

@StringRes
internal fun weatherIconStyleLabel(style: WeatherIconStyle): Int = when (style) {
    WeatherIconStyle.MONOCHROME -> R.string.weather_icon_style_monochrome
    WeatherIconStyle.COLORED -> R.string.weather_icon_style_colored
}
