package com.facetlauncher.app.ui.home.weather

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.facetlauncher.app.R
import com.facetlauncher.app.domain.WeatherCondition

@StringRes
internal fun conditionNameRes(condition: WeatherCondition): Int = when (condition) {
    WeatherCondition.CLEAR -> R.string.weather_condition_clear
    WeatherCondition.MOSTLY_CLEAR -> R.string.weather_condition_mostly_clear
    WeatherCondition.PARTLY_CLOUDY -> R.string.weather_condition_partly_cloudy
    WeatherCondition.CLOUDY -> R.string.weather_condition_cloudy
    WeatherCondition.FOG -> R.string.weather_condition_fog
    WeatherCondition.LIGHT_RAIN -> R.string.weather_condition_light_rain
    WeatherCondition.RAIN -> R.string.weather_condition_rain
    WeatherCondition.HEAVY_RAIN -> R.string.weather_condition_heavy_rain
    WeatherCondition.SLEET -> R.string.weather_condition_sleet
    WeatherCondition.SNOW -> R.string.weather_condition_snow
    WeatherCondition.THUNDERSTORM -> R.string.weather_condition_thunderstorm
}

@Composable
internal fun conditionName(condition: WeatherCondition): String = stringResource(conditionNameRes(condition))
