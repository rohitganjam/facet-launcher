package com.facetlauncher.app.domain

import com.facetlauncher.app.data.ClockStackRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.encode
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
    private val clockStackRepository = mock(ClockStackRepository::class.java)
    private val useCase = TurnOffWeatherUseCase(settingsRepository, facetRepository, UpdateClockConfigUseCase(settingsRepository, facetRepository, clockStackRepository))

    private fun facetWithClock(id: Long, pageId: Long, config: ClockWidgetConfig) = Facet(
        id = id, name = "Facet $id", position = id.toInt(),
        clockStack = listOf(ClockStackItem(id = pageId, facetId = id, position = 0, kind = ClockStackItemKind.CLOCK, nativeWidgetConfig = NativeWidgetConfig(clock = config).encode())),
    )

    @Test
    fun `turns weather off for the launcher and every clock that shows it`() = runTest {
        // Given one facet whose clock shows weather and one whose clock does not
        val showing = facetWithClock(1, pageId = 5, config = ClockWidgetConfig(showWeather = true, use24HourTime = true))
        val hiding = facetWithClock(2, pageId = 6, config = ClockWidgetConfig(showWeather = false))
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(facetRepository.observeFacets()).thenReturn(flowOf(listOf(showing, hiding)))
        `when`(facetRepository.getById(1)).thenReturn(showing)
        `when`(facetRepository.getById(2)).thenReturn(hiding)

        // When turning weather off
        useCase()

        // Then the launcher switch and the showing clock are off, its other settings kept, and the other clock is untouched
        verify(settingsRepository).setShowWeather(false)
        verify(clockStackRepository).setConfig(5, NativeWidgetConfig(clock = ClockWidgetConfig(showWeather = false, use24HourTime = true)))
        verify(clockStackRepository, never()).setConfig(6, NativeWidgetConfig(clock = ClockWidgetConfig(showWeather = false)))
    }
}
