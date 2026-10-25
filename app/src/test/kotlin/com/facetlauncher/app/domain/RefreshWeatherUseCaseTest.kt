package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DeviceLocation
import com.facetlauncher.app.data.LocationRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.data.model.WeatherPlace
import com.facetlauncher.app.data.places.City
import com.facetlauncher.app.data.places.CityRepository
import com.facetlauncher.app.data.places.GeocodedPlace
import com.facetlauncher.app.data.places.PlaceGeocoder
import com.facetlauncher.app.data.weather.PlaceNameSource
import com.facetlauncher.app.data.weather.RefreshOutcome
import com.facetlauncher.app.data.weather.WeatherFetchState
import com.facetlauncher.app.data.weather.WeatherRepository
import java.util.Locale
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class RefreshWeatherUseCaseTest {

    private val settingsRepository = mock(SettingsRepository::class.java)
    private val locationRepository = mock(LocationRepository::class.java)
    private val weatherRepository = mock(WeatherRepository::class.java)
    private val geocoder = mock(PlaceGeocoder::class.java)
    private val cities = mock(CityRepository::class.java)
    private val useCase = RefreshWeatherUseCase(settingsRepository, locationRepository, weatherRepository, ResolvePlaceUseCase(geocoder, cities))

    private val bengaluru = WeatherPlace(12.97, 77.59)
    private val pune = WeatherCity("Pune", 18.5204, 73.8567, "Asia/Kolkata")

    private fun settings(mode: WeatherLocationMode, city: WeatherCity? = null) {
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings(weatherLocationMode = mode, weatherCity = city)))
    }

    private fun storedState(state: WeatherFetchState) {
        `when`(weatherRepository.observeState()).thenReturn(flowOf(state))
    }

    // --- chosen city ---

    @Test
    fun `a chosen city is refreshed without touching the location`() = runTest {
        settings(WeatherLocationMode.CITY, pune)
        storedState(WeatherFetchState())
        `when`(weatherRepository.refresh(WeatherPlace(18.52, 73.86))).thenReturn(RefreshOutcome.Updated)

        val result = useCase()

        assertEquals(WeatherRefreshResult.Done(RefreshOutcome.Updated), result)
        verify(locationRepository, never()).currentLocation()
        verify(weatherRepository, never()).setPlaceName(WeatherPlace(18.52, 73.86), "Pune", PlaceNameSource.GEOCODER)
    }

    @Test
    fun `city mode without a city says so and makes no request`() = runTest {
        settings(WeatherLocationMode.CITY, null)

        assertEquals(WeatherRefreshResult.NoCity, useCase())
    }

    // --- current location ---

    @Test
    fun `current location without the permission says so and reads no position`() = runTest {
        settings(WeatherLocationMode.CURRENT)
        storedState(WeatherFetchState())
        `when`(locationRepository.isPermissionGranted()).thenReturn(false)

        assertEquals(WeatherRefreshResult.NeedsLocationPermission, useCase())
        verify(locationRepository, never()).currentLocation()
    }

    @Test
    fun `a fix with no earlier place is rounded and refreshed, then named`() = runTest {
        settings(WeatherLocationMode.CURRENT)
        storedState(WeatherFetchState())
        `when`(locationRepository.isPermissionGranted()).thenReturn(true)
        `when`(locationRepository.currentLocation()).thenReturn(DeviceLocation(12.9716, 77.5946, 920))
        val place = WeatherPlace(12.97, 77.59, 920)
        `when`(weatherRepository.refresh(place)).thenReturn(RefreshOutcome.Updated)
        // Given the weather state after the refresh holds the place but no name, and the geocoder knows it
        `when`(weatherRepository.observeState()).thenReturn(flowOf(WeatherFetchState()), flowOf(WeatherFetchState(place = place)))
        `when`(geocoder.reverse(12.97, 77.59, Locale.getDefault())).thenReturn(GeocodedPlace("Bengaluru", "Karnataka", "IN", 12.97, 77.59))

        val result = useCase()

        assertEquals(WeatherRefreshResult.Done(RefreshOutcome.Updated), result)
        verify(weatherRepository).setPlaceName(place, "Bengaluru", PlaceNameSource.GEOCODER)
    }

    @Test
    fun `staying within five kilometres keeps the stored place`() = runTest {
        // Given a stored place and a fix 2 km away
        settings(WeatherLocationMode.CURRENT)
        storedState(WeatherFetchState(place = bengaluru, placeName = "Bengaluru", placeNameSource = PlaceNameSource.GEOCODER))
        `when`(locationRepository.isPermissionGranted()).thenReturn(true)
        `when`(locationRepository.currentLocation()).thenReturn(DeviceLocation(12.988, 77.59, null))
        `when`(weatherRepository.refresh(bengaluru)).thenReturn(RefreshOutcome.UpToDate)

        val result = useCase()

        assertEquals(WeatherRefreshResult.Done(RefreshOutcome.UpToDate), result)
        verify(weatherRepository).refresh(bengaluru)
    }

    @Test
    fun `moving past five kilometres refreshes the new place`() = runTest {
        // Given a stored place and a fix in Pune
        settings(WeatherLocationMode.CURRENT)
        storedState(WeatherFetchState(place = bengaluru, placeName = "Bengaluru", placeNameSource = PlaceNameSource.GEOCODER))
        `when`(locationRepository.isPermissionGranted()).thenReturn(true)
        `when`(locationRepository.currentLocation()).thenReturn(DeviceLocation(18.5204, 73.8567, null))
        `when`(weatherRepository.refresh(bengaluru)).thenReturn(RefreshOutcome.UpToDate)
        `when`(weatherRepository.refresh(WeatherPlace(18.52, 73.86))).thenReturn(RefreshOutcome.Updated)

        val result = useCase()

        // Then the stored place was fetched without waiting for the fix, and the new place after it
        assertEquals(WeatherRefreshResult.Done(RefreshOutcome.Updated), result)
        verify(weatherRepository).refresh(bengaluru)
        verify(weatherRepository).refresh(WeatherPlace(18.52, 73.86))
    }

    @Test
    fun `a first fetch uses a recent last known position without waiting for a fresh fix`() = runTest {
        settings(WeatherLocationMode.CURRENT)
        storedState(WeatherFetchState())
        `when`(locationRepository.isPermissionGranted()).thenReturn(true)
        `when`(locationRepository.recentLocation(RefreshWeatherUseCase.RECENT_FIX_MS)).thenReturn(DeviceLocation(12.9716, 77.5946, null))
        `when`(weatherRepository.refresh(bengaluru)).thenReturn(RefreshOutcome.Updated)

        val result = useCase()

        assertEquals(WeatherRefreshResult.Done(RefreshOutcome.Updated), result)
        verify(locationRepository, never()).currentLocation()
    }

    @Test
    fun `no fix falls back to the stored place`() = runTest {
        settings(WeatherLocationMode.CURRENT)
        storedState(WeatherFetchState(place = bengaluru, placeName = "Bengaluru", placeNameSource = PlaceNameSource.GEOCODER))
        `when`(locationRepository.isPermissionGranted()).thenReturn(true)
        `when`(locationRepository.currentLocation()).thenReturn(null)
        `when`(weatherRepository.refresh(bengaluru)).thenReturn(RefreshOutcome.TooSoon(java.time.Instant.EPOCH))

        val result = useCase()

        assertEquals(WeatherRefreshResult.Done(RefreshOutcome.TooSoon(java.time.Instant.EPOCH)), result)
    }

    @Test
    fun `no fix and no stored place says there is no location`() = runTest {
        settings(WeatherLocationMode.CURRENT)
        storedState(WeatherFetchState())
        `when`(locationRepository.isPermissionGranted()).thenReturn(true)
        `when`(locationRepository.currentLocation()).thenReturn(null)

        assertEquals(WeatherRefreshResult.NoLocation, useCase())
    }

    @Test
    fun `an offline name is looked up again but a geocoder name is left alone`() = runTest {
        // Given the stored place already has a geocoder name
        settings(WeatherLocationMode.CURRENT)
        storedState(WeatherFetchState(place = bengaluru, placeName = "Bengaluru", placeNameSource = PlaceNameSource.GEOCODER))
        `when`(locationRepository.isPermissionGranted()).thenReturn(true)
        `when`(locationRepository.currentLocation()).thenReturn(DeviceLocation(12.97, 77.59, null))
        `when`(weatherRepository.refresh(bengaluru)).thenReturn(RefreshOutcome.UpToDate)

        useCase()

        // Then the geocoder is not asked and no name is written
        verify(geocoder, never()).reverse(12.97, 77.59, Locale.getDefault())
        verify(weatherRepository, never()).setPlaceName(bengaluru, "Bengaluru", PlaceNameSource.GEOCODER)
    }

    @Test
    fun `an offline name is retried and replaced when the geocoder now answers`() = runTest {
        settings(WeatherLocationMode.CURRENT)
        storedState(WeatherFetchState(place = bengaluru, placeName = "Bengaluru", placeNameSource = PlaceNameSource.OFFLINE))
        `when`(locationRepository.isPermissionGranted()).thenReturn(true)
        `when`(locationRepository.currentLocation()).thenReturn(DeviceLocation(12.97, 77.59, null))
        `when`(weatherRepository.refresh(bengaluru)).thenReturn(RefreshOutcome.UpToDate)
        `when`(geocoder.reverse(12.97, 77.59, Locale.getDefault())).thenReturn(GeocodedPlace("ಬೆಂಗಳೂರು", null, "IN", 12.97, 77.59))
        `when`(cities.nearest(12.97, 77.59, "IN", ResolvePlaceUseCase.MAX_CITY_KM)).thenReturn(City("Bengaluru", "Karnataka", "IN", 12.97, 77.59, 1, "Asia/Kolkata"))

        useCase()

        verify(weatherRepository).setPlaceName(bengaluru, "ಬೆಂಗಳೂರು", PlaceNameSource.GEOCODER)
    }
}
