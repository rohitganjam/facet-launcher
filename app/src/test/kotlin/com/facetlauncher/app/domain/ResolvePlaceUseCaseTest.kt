package com.facetlauncher.app.domain

import com.facetlauncher.app.data.places.City
import com.facetlauncher.app.data.places.CityRepository
import com.facetlauncher.app.data.places.GeocodedPlace
import com.facetlauncher.app.data.places.PlaceGeocoder
import com.facetlauncher.app.data.weather.PlaceNameSource
import java.util.Locale
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ResolvePlaceUseCaseTest {

    private val geocoder = mock(PlaceGeocoder::class.java)
    private val cities = mock(CityRepository::class.java)
    private val useCase = ResolvePlaceUseCase(geocoder, cities)

    private val locale = Locale.forLanguageTag("en-IN")
    private val bengaluru = City("Bengaluru", "Karnataka", "IN", 12.9716, 77.5946, 8443675, "Asia/Kolkata")

    @Test
    fun `the geocoder's name wins and the nearest city supplies the timezone`() = runTest {
        `when`(geocoder.reverse(12.97, 77.59, locale)).thenReturn(GeocodedPlace("Bengaluru", "Karnataka", "IN", 12.97, 77.59))
        `when`(cities.nearest(12.97, 77.59, "IN", ResolvePlaceUseCase.MAX_CITY_KM)).thenReturn(bengaluru)

        val resolved = useCase(12.97, 77.59, locale)

        assertEquals(ResolvedPlace("Bengaluru", PlaceNameSource.GEOCODER, "Asia/Kolkata"), resolved)
    }

    @Test
    fun `without a geocoder answer the nearest city names the place offline`() = runTest {
        `when`(geocoder.reverse(12.97, 77.59, locale)).thenReturn(null)
        `when`(cities.nearest(12.97, 77.59, null, ResolvePlaceUseCase.MAX_CITY_KM)).thenReturn(bengaluru)

        val resolved = useCase(12.97, 77.59, locale)

        assertEquals(ResolvedPlace("Bengaluru", PlaceNameSource.OFFLINE, "Asia/Kolkata"), resolved)
    }

    @Test
    fun `no city in the geocoder's country falls back to the nearest city anywhere`() = runTest {
        `when`(geocoder.reverse(12.97, 77.59, locale)).thenReturn(GeocodedPlace("Somewhere", null, "ZZ", 12.97, 77.59))
        `when`(cities.nearest(12.97, 77.59, "ZZ", ResolvePlaceUseCase.MAX_CITY_KM)).thenReturn(null)
        `when`(cities.nearest(12.97, 77.59, null, ResolvePlaceUseCase.MAX_CITY_KM)).thenReturn(bengaluru)

        val resolved = useCase(12.97, 77.59, locale)

        assertEquals(ResolvedPlace("Somewhere", PlaceNameSource.GEOCODER, "Asia/Kolkata"), resolved)
    }

    @Test
    fun `the geocoder's name is kept even when no city is near enough for a timezone`() = runTest {
        `when`(geocoder.reverse(1.0, 2.0, locale)).thenReturn(GeocodedPlace("Middle of nowhere", null, null, 1.0, 2.0))

        val resolved = useCase(1.0, 2.0, locale)

        assertEquals(ResolvedPlace("Middle of nowhere", PlaceNameSource.GEOCODER, null), resolved)
    }

    @Test
    fun `nothing answering gives no name and no timezone`() = runTest {
        assertEquals(ResolvedPlace(null, null, null), useCase(1.0, 2.0, locale))
    }
}
