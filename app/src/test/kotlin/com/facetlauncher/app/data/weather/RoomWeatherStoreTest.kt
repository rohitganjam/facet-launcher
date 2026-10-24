package com.facetlauncher.app.data.weather

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.local.WeatherMetaEntity
import com.facetlauncher.app.data.model.WeatherHour
import com.facetlauncher.app.data.model.WeatherPlace
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomWeatherStoreTest {

    private fun createStore(): Pair<RoomWeatherStore, FacetDatabase> {
        val database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        return RoomWeatherStore(database, database.weatherDao()) to database
    }

    private fun hour(time: String, temperature: Double, symbol: String? = null) =
        WeatherHour(Instant.parse(time), temperature, 70.0, 1010.0, 90.0, 2.5, 120.0, 0.4, symbol)

    @Test
    fun `an empty store has no hours and a blank fetch state`() = runTest {
        val (store, _) = createStore()

        assertEquals(emptyList<WeatherHour>(), store.readHours())
        assertEquals(WeatherFetchState(), store.readState())
    }

    @Test
    fun `hours round-trip with every field and come back in time order`() = runTest {
        val (store, _) = createStore()
        val later = hour("2026-10-24T06:00:00Z", 27.0, "cloudy")
        val earlier = hour("2026-10-24T05:00:00Z", 26.0, "heavyrain")

        store.replaceHours(listOf(later, earlier))

        assertEquals(listOf(earlier, later), store.readHours())
        assertEquals(listOf(earlier, later), store.observeHours().first())
    }

    @Test
    fun `replacing hours drops the ones that are not in the new list`() = runTest {
        val (store, _) = createStore()
        store.replaceHours(listOf(hour("2026-10-24T05:00:00Z", 26.0), hour("2026-10-24T06:00:00Z", 27.0)))

        store.replaceHours(listOf(hour("2026-10-24T06:00:00Z", 28.0)))

        assertEquals(listOf(28.0), store.readHours().map { it.temperatureC })
    }

    @Test
    fun `a missing temperature-less field is stored as null`() = runTest {
        val (store, _) = createStore()
        val sparse = WeatherHour(Instant.parse("2026-10-27T00:00:00Z"), 21.2, null, null, null, null, null, null, null)

        store.replaceHours(listOf(sparse))

        assertEquals(listOf(sparse), store.readHours())
    }

    @Test
    fun `fetch state round-trips`() = runTest {
        val (store, _) = createStore()
        val state = WeatherFetchState(
            place = WeatherPlace(12.97, 77.59, 920),
            fetchedAt = Instant.parse("2026-10-24T05:30:00Z"),
            expiresAt = Instant.parse("2026-10-24T06:30:00Z"),
            lastModified = "Sat, 24 Oct 2026 05:21:00 GMT",
            lastAttemptAt = Instant.parse("2026-10-24T05:30:00Z"),
            blockedUntil = Instant.parse("2026-10-24T06:00:00Z"),
        )

        store.writeState(state)

        assertEquals(state, store.readState())
        assertEquals(state, store.observeState().first())
    }

    @Test
    fun `the stored place name survives a state write for the same place`() = runTest {
        val (store, database) = createStore()
        val place = WeatherPlace(12.97, 77.59)
        store.writeState(WeatherFetchState(place = place))
        val dao = database.weatherDao()
        dao.upsertMeta(dao.getMeta()!!.copy(placeName = "Bengaluru", placeNameSource = "GEOCODER"))

        store.writeState(WeatherFetchState(place = place, lastAttemptAt = Instant.parse("2026-10-24T05:30:00Z")))

        assertEquals("Bengaluru", dao.getMeta()!!.placeName)
        assertEquals("GEOCODER", dao.getMeta()!!.placeNameSource)
    }

    @Test
    fun `the stored place name is dropped when the place changes`() = runTest {
        val (store, database) = createStore()
        store.writeState(WeatherFetchState(place = WeatherPlace(12.97, 77.59)))
        val dao = database.weatherDao()
        dao.upsertMeta(dao.getMeta()!!.copy(placeName = "Bengaluru", placeNameSource = "GEOCODER"))

        store.writeState(WeatherFetchState(place = WeatherPlace(18.52, 73.86)))

        assertNull(dao.getMeta()!!.placeName)
        assertNull(dao.getMeta()!!.placeNameSource)
    }

    @Test
    fun `a meta row with no coordinates reads as no place`() = runTest {
        val (store, database) = createStore()
        database.weatherDao().upsertMeta(WeatherMetaEntity())

        assertNull(store.readState().place)
    }
}
