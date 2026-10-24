package com.facetlauncher.app.data.places

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Reads the real `assets/cities.tsv` that ships in the app. */
@RunWith(RobolectricTestRunner::class)
class CityRepositoryTest {

    private val repository = CityRepository(ApplicationProvider.getApplicationContext(), Dispatchers.Unconfined)

    @Test
    fun `the bundled list finds the cities this app is built around`() = runTest {
        val bengaluru = repository.search("bengaluru").first()
        val pune = repository.search("pune").first()

        assertEquals("IN", bengaluru.countryCode)
        assertEquals("Asia/Kolkata", bengaluru.timezone)
        assertEquals("Karnataka", bengaluru.admin)
        assertEquals("Maharashtra", pune.admin)
    }

    @Test
    fun `a point in Bengaluru is nearest to Bengaluru, in India`() = runTest {
        val city = repository.nearest(12.97, 77.59, countryCode = "IN", maxKm = 50.0)

        assertEquals("Bengaluru", city!!.name)
    }

    @Test
    fun `a point in the middle of the ocean has no city within 300 km`() = runTest {
        assertNull(repository.nearest(-40.0, -120.0, maxKm = 300.0))
    }

    @Test
    fun `the list is large and every row has a timezone Java knows`() = runTest {
        val all = repository.search("a", limit = Int.MAX_VALUE)

        assertTrue("expected tens of thousands of cities, got ${all.size}", all.size > 20_000)
        val unknown = all.map { it.timezone }.distinct().filter { runCatching { java.time.ZoneId.of(it) }.isFailure }
        assertEquals(emptyList<String>(), unknown)
    }

    @Test
    fun `accents and case do not matter for a real name`() = runTest {
        assertTrue(repository.search("zurich").any { it.countryCode == "CH" })
        assertTrue(repository.search("ZÜRICH").any { it.countryCode == "CH" })
    }
}
