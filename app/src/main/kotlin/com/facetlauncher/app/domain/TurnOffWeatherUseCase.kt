package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Turns weather off for every facet's clock (and the launcher's own switch), so nothing keeps asking for a place. */
class TurnOffWeatherUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val updateClockConfig: UpdateClockConfigUseCase,
) {
    suspend operator fun invoke() {
        settingsRepository.setShowWeather(false)
        val settings = settingsRepository.settings.first()
        facetRepository.observeFacets().first()
            .filter { effectiveClockConfig(it, settings).showWeather }
            .forEach { facet -> updateClockConfig(facet.id) { it.copy(showWeather = false) } }
    }
}
