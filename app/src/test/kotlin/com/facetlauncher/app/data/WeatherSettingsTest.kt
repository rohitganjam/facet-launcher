package com.facetlauncher.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.facetlauncher.app.data.model.PressureUnit
import com.facetlauncher.app.data.model.RainUnit
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.MAX_SAVED_WEATHER_CITIES
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.decodeWeatherCities
import com.facetlauncher.app.data.model.encodeWeatherCities
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.data.model.WindSpeedUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class WeatherSettingsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createDataStore(): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(produceFile = { tempFolder.newFile("test-${System.nanoTime()}.preferences_pb") })

    private val pune = WeatherCity("Pune", 18.52, 73.86, "Asia/Kolkata")

    @Test
    fun `accessories and weather default to alarm and battery on, weather off, units automatic`() = runTest {
        val settings = SettingsRepository(createDataStore()).settings.first()

        assertTrue(settings.showNextAlarm)
        assertTrue(settings.showBatteryPercentage)
        assertFalse(settings.showWeather)
        assertEquals(TemperatureUnit.AUTO, settings.temperatureUnit)
        assertEquals(WindSpeedUnit.AUTO, settings.windSpeedUnit)
        assertEquals(RainUnit.AUTO, settings.rainUnit)
        assertEquals(PressureUnit.AUTO, settings.pressureUnit)
        assertEquals(WeatherLocationMode.CURRENT, settings.weatherLocationMode)
        assertEquals(WeatherIconStyle.MONOCHROME, settings.weatherIconStyle)
        assertNull(settings.weatherCity)
    }

    @Test
    fun `the three accessory switches round-trip independently`() = runTest {
        val repository = SettingsRepository(createDataStore())

        repository.setShowNextAlarm(false)
        repository.setShowWeather(true)

        val settings = repository.settings.first()
        assertFalse(settings.showNextAlarm)
        assertTrue(settings.showBatteryPercentage)
        assertTrue(settings.showWeather)
    }

    @Test
    fun `each unit round-trips`() = runTest {
        val repository = SettingsRepository(createDataStore())

        repository.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
        repository.setWindSpeedUnit(WindSpeedUnit.MPH)
        repository.setRainUnit(RainUnit.INCH)
        repository.setPressureUnit(PressureUnit.INHG)

        val settings = repository.settings.first()
        assertEquals(TemperatureUnit.FAHRENHEIT, settings.temperatureUnit)
        assertEquals(WindSpeedUnit.MPH, settings.windSpeedUnit)
        assertEquals(RainUnit.INCH, settings.rainUnit)
        assertEquals(PressureUnit.INHG, settings.pressureUnit)
    }

    @Test
    fun `an unknown unit stored by another version falls back to automatic`() = runTest {
        val dataStore = createDataStore()
        dataStore.edit { it[stringPreferencesKey("wind_speed_unit")] = "KNOTS" }

        assertEquals(WindSpeedUnit.AUTO, SettingsRepository(dataStore).settings.first().windSpeedUnit)
    }

    @Test
    fun `the chosen city and mode round-trip, and clearing the city removes it`() = runTest {
        val repository = SettingsRepository(createDataStore())

        repository.setWeatherLocationMode(WeatherLocationMode.CITY)
        repository.setWeatherCity(pune)
        assertEquals(WeatherLocationMode.CITY, repository.settings.first().weatherLocationMode)
        assertEquals(pune, repository.settings.first().weatherCity)

        repository.setWeatherCity(null)
        assertNull(repository.settings.first().weatherCity)
    }

    @Test
    fun `a city missing one of its fields reads as none`() = runTest {
        val dataStore = createDataStore()
        dataStore.edit { it[stringPreferencesKey("weather_city_name")] = "Pune" }

        assertNull(SettingsRepository(dataStore).settings.first().weatherCity)
    }

    private val london = WeatherCity("London", 51.51, -0.13, "Europe/London")

    @Test
    fun `choosing a city saves it, selects it and switches to city mode`() = runTest {
        val repository = SettingsRepository(createDataStore())

        assertTrue(repository.chooseWeatherCity(pune))
        assertTrue(repository.chooseWeatherCity(london))

        val settings = repository.settings.first()
        assertEquals(listOf(pune, london), settings.savedWeatherCities)
        assertEquals(london, settings.weatherCity)
        assertEquals(WeatherLocationMode.CITY, settings.weatherLocationMode)
    }

    @Test
    fun `choosing a saved city again selects it without adding a copy`() = runTest {
        val repository = SettingsRepository(createDataStore())
        repository.chooseWeatherCity(pune)
        repository.chooseWeatherCity(london)

        repository.chooseWeatherCity(pune)

        val settings = repository.settings.first()
        assertEquals(listOf(pune, london), settings.savedWeatherCities)
        assertEquals(pune, settings.weatherCity)
    }

    @Test
    fun `a new city is refused once the saved list is full`() = runTest {
        val repository = SettingsRepository(createDataStore())
        val cities = (1..MAX_SAVED_WEATHER_CITIES).map { WeatherCity("City $it", it.toDouble(), it.toDouble(), "UTC") }
        cities.forEach { repository.chooseWeatherCity(it) }

        assertFalse(repository.chooseWeatherCity(pune))

        val settings = repository.settings.first()
        assertEquals(cities, settings.savedWeatherCities)
        assertEquals(cities.last(), settings.weatherCity)
        assertTrue(repository.chooseWeatherCity(cities.first()))
    }

    @Test
    fun `removing the chosen city leaves none chosen and keeps the others`() = runTest {
        val repository = SettingsRepository(createDataStore())
        repository.chooseWeatherCity(pune)
        repository.chooseWeatherCity(london)

        repository.removeWeatherCity(london)

        val settings = repository.settings.first()
        assertEquals(listOf(pune), settings.savedWeatherCities)
        assertNull(settings.weatherCity)
        assertEquals(WeatherLocationMode.CITY, settings.weatherLocationMode)
    }

    @Test
    fun `removing another city keeps the chosen one`() = runTest {
        val repository = SettingsRepository(createDataStore())
        repository.chooseWeatherCity(pune)
        repository.chooseWeatherCity(london)

        repository.removeWeatherCity(pune)

        assertEquals(london, repository.settings.first().weatherCity)
    }

    @Test
    fun `a city chosen before the list existed shows in the list`() = runTest {
        val repository = SettingsRepository(createDataStore())

        repository.setWeatherCity(pune)

        assertEquals(listOf(pune), repository.settings.first().savedWeatherCities)
    }

    @Test
    fun `saved cities survive the stored string, whatever the name holds`() {
        val cities = listOf(pune, WeatherCity("Odd\tName\nCity", 1.0, 2.0, "UTC"))

        val decoded = decodeWeatherCities(encodeWeatherCities(cities))

        assertEquals(listOf(pune, WeatherCity("Odd Name City", 1.0, 2.0, "UTC")), decoded)
        assertEquals(emptyList<WeatherCity>(), decodeWeatherCities("garbage\nonly\tthree\tfields"))
    }

    @Test
    fun `the weather icon style round-trips`() = runTest {
        val repository = SettingsRepository(createDataStore())

        repository.setWeatherIconStyle(WeatherIconStyle.COLORED)

        assertEquals(WeatherIconStyle.COLORED, repository.settings.first().weatherIconStyle)
    }
}
