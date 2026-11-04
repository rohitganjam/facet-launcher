package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.WidgetWeatherLocation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class ObserveClockWeatherUseCaseTest {

    private val oslo = WidgetWeatherLocation.City(WeatherCity("Oslo", 59.91, 10.75, "Europe/Oslo"))
    private val launcherWeather = mock(WeatherState::class.java)
    private val ownWeather = mock(WeatherState::class.java)

    private val settings = mock(SettingsRepository::class.java)
    private val facets = mock(FacetRepository::class.java)
    private val observeWeather = mock(ObserveWeatherUseCase::class.java)
    private val resolver = mock(NativeWidgetOverrideResolver::class.java)

    private fun useCase(facet: Facet?): ObserveClockWeatherUseCase {
        `when`(settings.settings).thenReturn(flowOf(LauncherSettings(activeFacetId = 1)))
        `when`(facets.observeFacets()).thenReturn(flowOf(listOfNotNull(facet)))
        `when`(observeWeather.invoke()).thenReturn(flowOf(launcherWeather))
        return ObserveClockWeatherUseCase(settings, facets, observeWeather, resolver)
    }

    private fun facet(overrideClock: Boolean, location: WidgetWeatherLocation?) =
        Facet(id = 1, name = "Work", position = 0, overrideClock = overrideClock, clockWeatherLocation = location)

    /** Registers the matcher but returns a real value: `any` itself hands back `null`, which a non-null Kotlin parameter rejects. */
    private fun anySettings(): LauncherSettings {
        any(LauncherSettings::class.java)
        return LauncherSettings()
    }

    private suspend fun givenResolverReturns(override: NativeWidgetOverride?) {
        val result: Map<Int, NativeWidgetOverride> = override?.let { mapOf(0 to it) }.orEmpty()
        `when`(resolver.resolve(anyList<NativeWidgetSetting<Int>>(), anySettings())).thenReturn(result)
    }

    @Test
    fun `a facet with no place of its own shows the launcher's forecast and asks nothing else`() = runTest {
        // Given a facet that follows Weather settings
        val clock = useCase(facet(overrideClock = true, location = null)).invoke().first()

        // Then the launcher's forecast is shown, and it is not marked as the facet's own
        assertSame(launcherWeather, clock.weather)
        assertEquals(false, clock.own)
        verify(resolver, never()).resolve(anyList<NativeWidgetSetting<Int>>(), anySettings())
    }

    @Test
    fun `a facet overriding the clock with its own city shows that city's forecast`() = runTest {
        // Given a facet overriding the clock with Oslo, and the widget path holding Oslo's forecast
        givenResolverReturns(NativeWidgetOverride(weather = ownWeather))
        val clock = useCase(facet(overrideClock = true, location = oslo)).invoke().first()

        // Then it is Oslo's, flagged as the facet's own
        assertSame(ownWeather, clock.weather)
        assertEquals(true, clock.own)
    }

    @Test
    fun `a facet that inherits the clock ignores a place it once chose`() = runTest {
        // Given a facet that is not overriding the clock but still holds a stored city
        val clock = useCase(facet(overrideClock = false, location = oslo)).invoke().first()

        // Then the launcher's forecast is shown and the widget path is never asked
        assertSame(launcherWeather, clock.weather)
        verify(resolver, never()).resolve(anyList<NativeWidgetSetting<Int>>(), anySettings())
    }

    @Test
    fun `when the launcher's forecast already covers the facet's place it is used and not marked as its own`() = runTest {
        // Given the widget path saying "follow the launcher" for this place
        givenResolverReturns(null)
        val clock = useCase(facet(overrideClock = true, location = oslo)).invoke().first()

        // Then the launcher's forecast is shown, so the one request serves both
        assertSame(launcherWeather, clock.weather)
        assertEquals(false, clock.own)
    }

    @Test
    fun `a facet's own place without a forecast says why`() = runTest {
        // Given the position the facet wants is not allowed
        givenResolverReturns(NativeWidgetOverride(weatherUnavailable = true, locationMissing = true))
        val clock = useCase(facet(overrideClock = true, location = WidgetWeatherLocation.Current)).invoke().first()

        // Then there is no forecast, it is the facet's own, and the reason travels with it
        assertEquals(null, clock.weather)
        assertEquals(true, clock.own)
        assertEquals(true, clock.unavailable)
        assertEquals(true, clock.locationMissing)
    }

    @Test
    fun `with no active facet the launcher's forecast is shown`() = runTest {
        // Given no facet at all
        val clock = useCase(null).invoke().first()

        // Then the launcher's forecast is shown
        assertSame(launcherWeather, clock.weather)
    }
}
