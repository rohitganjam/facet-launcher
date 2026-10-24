package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Turns weather off for the launcher default and for every facet, so nothing keeps asking for a place. */
class TurnOffWeatherUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
) {
    suspend operator fun invoke() {
        settingsRepository.setShowWeather(false)
        facetRepository.observeFacets().first().filter { it.clockShowWeather }.forEach { facetRepository.setClockShowWeather(it.id, false) }
    }
}
