package com.facetlauncher.app.data.model

/**
 * Reserved shape for the clock's weather accessory item — not yet backed by a repository or any
 * real data source. Unused everywhere; exists only so the clock templates' weather slot has a
 * type to reserve without wiring it up.
 */
data class WeatherInfo(
    val temperatureCelsius: Int,
)
