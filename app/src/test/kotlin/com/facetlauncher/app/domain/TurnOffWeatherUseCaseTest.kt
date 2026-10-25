package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.Facet
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class TurnOffWeatherUseCaseTest {

    private val settingsRepository = mock(SettingsRepository::class.java)
    private val facetRepository = mock(FacetRepository::class.java)
    private val useCase = TurnOffWeatherUseCase(settingsRepository, facetRepository)

    @Test
    fun `turns weather off for the default and every facet that shows it`() = runTest {
        // Given one facet showing weather and one not
        val showing = Facet(id = 1, name = "Work", position = 0, clockShowWeather = true)
        val hiding = Facet(id = 2, name = "Home", position = 1, clockShowWeather = false)
        `when`(facetRepository.observeFacets()).thenReturn(flowOf(listOf(showing, hiding)))

        // When turning weather off
        useCase()

        // Then the default and the showing facet are switched off, the other left alone
        verify(settingsRepository).setShowWeather(false)
        verify(facetRepository).setClockShowWeather(1, false)
        verify(facetRepository, never()).setClockShowWeather(2, false)
    }
}
