package com.facetlauncher.app.ui.home.weather

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import com.facetlauncher.app.R
import com.facetlauncher.app.domain.conditionOf
import com.facetlauncher.app.domain.isNightSymbol
import com.facetlauncher.app.ui.home.HomeWeatherState
import com.facetlauncher.app.ui.home.clock.ClockWeatherAccessory

/** The clock's weather accessory for [weather], or `null` for none: a picture and "27°", with the spoken text and the tap. */
@Composable
fun rememberClockWeatherAccessory(weather: HomeWeatherState?, onClick: () -> Unit): ClockWeatherAccessory? {
    val condition = weather?.let { conditionOf(it.symbolCode, it.precipitationMm) }
    val temperature = weather?.let { temperatureText(it.temperatureC, it.temperatureUnit) }
    val conditionText = condition?.let { conditionName(it) }
    val description = when {
        temperature == null -> null
        conditionText == null -> stringResource(R.string.weather_accessory_description_no_condition, temperature)
        else -> stringResource(R.string.weather_accessory_description, temperature, conditionText)
    }
    val currentOnClick by rememberUpdatedState(onClick)
    val kind = weatherIconKind(condition, isNightSymbol(weather?.symbolCode))
    return remember(temperature, description, kind) {
        if (temperature == null || description == null) null else ClockWeatherAccessory(kind, temperature, description) { currentOnClick() }
    }
}
