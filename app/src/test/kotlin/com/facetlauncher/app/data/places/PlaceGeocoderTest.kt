package com.facetlauncher.app.data.places

import android.location.Address
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// Robolectric: `Address` is an Android framework class.
@RunWith(RobolectricTestRunner::class)
class PlaceGeocoderTest {

    private fun address(subLocality: String? = null, locality: String? = null, subAdmin: String? = null, admin: String? = null) =
        Address(Locale.ENGLISH).apply {
            this.subLocality = subLocality
            this.locality = locality
            this.subAdminArea = subAdmin
            this.adminArea = admin
            latitude = 12.972
            longitude = 77.595
        }

    @Test
    fun `a reverse lookup names the neighbourhood and the city`() {
        val place = address(subLocality = "Indiranagar", locality = "Bengaluru", admin = "Karnataka").toGeocodedPlace(withNeighbourhood = true)

        assertEquals("Indiranagar, Bengaluru", place?.name)
    }

    @Test
    fun `a search keeps the plain city name`() {
        val place = address(subLocality = "Indiranagar", locality = "Bengaluru").toGeocodedPlace(withNeighbourhood = false)

        assertEquals("Bengaluru", place?.name)
    }

    @Test
    fun `no neighbourhood falls back to the city then the wider areas`() {
        assertEquals("Bengaluru", address(locality = "Bengaluru").toGeocodedPlace(true)?.name)
        assertEquals("Bangalore Urban", address(subAdmin = "Bangalore Urban", admin = "Karnataka").toGeocodedPlace(true)?.name)
        assertEquals("Karnataka", address(admin = "Karnataka").toGeocodedPlace(true)?.name)
    }

    @Test
    fun `a neighbourhood that repeats the city is not doubled`() {
        assertEquals("Bengaluru", address(subLocality = "bengaluru", locality = "Bengaluru").toGeocodedPlace(true)?.name)
    }

    @Test
    fun `a neighbourhood alone is used when the city is missing`() {
        assertEquals("Indiranagar", address(subLocality = "Indiranagar").toGeocodedPlace(true)?.name)
    }

    @Test
    fun `an address with no name or no position is dropped`() {
        assertNull(address().toGeocodedPlace(true))
        assertNull(Address(Locale.ENGLISH).apply { locality = "Bengaluru" }.toGeocodedPlace(true))
    }
}
