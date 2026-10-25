package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.places.City
import com.facetlauncher.app.data.places.CityRepository
import com.facetlauncher.app.data.places.GeocodedPlace
import com.facetlauncher.app.data.places.PlaceGeocoder
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class SearchPlacesUseCaseTest {

    private val geocoder = mock(PlaceGeocoder::class.java)
    private val cities = mock(CityRepository::class.java)
    private val useCase = SearchPlacesUseCase(geocoder, cities)

    private val locale = Locale.forLanguageTag("en-IN")
    private val pune = City("Pune", "Maharashtra", "IN", 18.5204, 73.8567, 3124458, "Asia/Kolkata")
    private val punjab = City("Punjab Village", "", "IN", 30.9, 75.8, 12000, "Asia/Kolkata")

    @Test
    fun `a blank query finds nothing and asks nobody`() = runTest {
        assertEquals(emptyList<PlaceCandidate>(), useCase("   ", locale))
    }

    @Test
    fun `with no geocoder the list answers`() = runTest {
        `when`(geocoder.search("pun", locale, 10)).thenReturn(emptyList())
        `when`(cities.search("pun", 10)).thenReturn(listOf(pune))

        val found = useCase("pun", locale)

        assertEquals(listOf(PlaceCandidate("Pune", "Maharashtra", "IN", 18.5204, 73.8567, "Asia/Kolkata")), found)
    }

    @Test
    fun `a geocoder result comes first, with the zone of the nearest listed city in its country`() = runTest {
        `when`(geocoder.search("pun", locale, 10)).thenReturn(listOf(GeocodedPlace("पुणे", "महाराष्ट्र", "IN", 18.52, 73.85)))
        `when`(cities.nearest(18.52, 73.85, "IN", ResolvePlaceUseCase.MAX_CITY_KM)).thenReturn(pune)
        `when`(cities.search("pun", 10)).thenReturn(listOf(pune, punjab))

        val found = useCase("pun", locale)

        // Then the localised result is first, and the list's Pune (the same place, within 10 km) is not repeated
        assertEquals(listOf("पुणे", "Punjab Village"), found.map { it.name })
        assertEquals("Asia/Kolkata", found[0].timezone)
    }

    @Test
    fun `a geocoder result with no listed city near uses the device's zone`() = runTest {
        `when`(geocoder.search("remote", locale, 10)).thenReturn(listOf(GeocodedPlace("Remote", null, "ZZ", 1.0, 2.0)))
        `when`(cities.search("remote", 10)).thenReturn(emptyList())

        val found = useCase("remote", locale)

        assertEquals(ZoneId.systemDefault().id, found.single().timezone)
    }

    @Test
    fun `the limit applies to the merged list`() = runTest {
        `when`(geocoder.search("a", locale, 2)).thenReturn(listOf(GeocodedPlace("A1", null, null, 10.0, 10.0), GeocodedPlace("A2", null, null, 20.0, 20.0)))
        `when`(cities.search("a", 2)).thenReturn(listOf(pune))

        assertEquals(2, useCase("a", locale, limit = 2).size)
    }

    @Test
    fun `a candidate becomes the city that is stored`() {
        val candidate = PlaceCandidate("Pune", "Maharashtra", "IN", 18.5204, 73.8567, "Asia/Kolkata")

        assertEquals(WeatherCity("Pune", 18.5204, 73.8567, "Asia/Kolkata"), candidate.toWeatherCity())
    }
}
