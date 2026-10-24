package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.PressureUnit
import com.facetlauncher.app.data.model.RainUnit
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.WindSpeedUnit
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherUnitsTest {

    private val india = Locale.forLanguageTag("en-IN")
    private val us = Locale.forLanguageTag("en-US")
    private val uk = Locale.forLanguageTag("en-GB")

    @Test
    fun `automatic units follow the locale's country`() {
        assertEquals(TemperatureUnit.CELSIUS, WeatherUnits.resolve(TemperatureUnit.AUTO, india))
        assertEquals(TemperatureUnit.FAHRENHEIT, WeatherUnits.resolve(TemperatureUnit.AUTO, us))
        assertEquals(WindSpeedUnit.KMH, WeatherUnits.resolve(WindSpeedUnit.AUTO, india))
        assertEquals(WindSpeedUnit.MPH, WeatherUnits.resolve(WindSpeedUnit.AUTO, us))
        assertEquals(WindSpeedUnit.MPH, WeatherUnits.resolve(WindSpeedUnit.AUTO, uk))
        assertEquals(RainUnit.MM, WeatherUnits.resolve(RainUnit.AUTO, uk))
        assertEquals(RainUnit.INCH, WeatherUnits.resolve(RainUnit.AUTO, us))
        assertEquals(PressureUnit.HPA, WeatherUnits.resolve(PressureUnit.AUTO, uk))
        assertEquals(PressureUnit.INHG, WeatherUnits.resolve(PressureUnit.AUTO, us))
    }

    @Test
    fun `a locale with no country falls back to metric`() {
        assertEquals(TemperatureUnit.CELSIUS, WeatherUnits.resolve(TemperatureUnit.AUTO, Locale.ENGLISH))
        assertEquals(WindSpeedUnit.KMH, WeatherUnits.resolve(WindSpeedUnit.AUTO, Locale.ENGLISH))
    }

    @Test
    fun `an explicit choice wins over the locale`() {
        assertEquals(TemperatureUnit.CELSIUS, WeatherUnits.resolve(TemperatureUnit.CELSIUS, us))
        assertEquals(WindSpeedUnit.MS, WeatherUnits.resolve(WindSpeedUnit.MS, us))
        assertEquals(RainUnit.MM, WeatherUnits.resolve(RainUnit.MM, us))
        assertEquals(PressureUnit.MMHG, WeatherUnits.resolve(PressureUnit.MMHG, india))
    }

    @Test
    fun `temperature converts to Fahrenheit and leaves Celsius alone`() {
        assertEquals(80.6, WeatherUnits.temperature(27.0, TemperatureUnit.FAHRENHEIT), 0.001)
        assertEquals(32.0, WeatherUnits.temperature(0.0, TemperatureUnit.FAHRENHEIT), 0.001)
        assertEquals(27.0, WeatherUnits.temperature(27.0, TemperatureUnit.CELSIUS), 0.001)
    }

    @Test
    fun `wind converts from metres per second`() {
        assertEquals(10.44, WeatherUnits.windSpeed(2.9, WindSpeedUnit.KMH), 0.001)
        assertEquals(6.487, WeatherUnits.windSpeed(2.9, WindSpeedUnit.MPH), 0.001)
        assertEquals(2.9, WeatherUnits.windSpeed(2.9, WindSpeedUnit.MS), 0.001)
    }

    @Test
    fun `rain converts to inches`() {
        assertEquals(1.0, WeatherUnits.rain(25.4, RainUnit.INCH), 0.0001)
        assertEquals(25.4, WeatherUnits.rain(25.4, RainUnit.MM), 0.0001)
    }

    @Test
    fun `pressure converts to inches and millimetres of mercury`() {
        assertEquals(29.92, WeatherUnits.pressure(1013.25, PressureUnit.INHG), 0.01)
        assertEquals(760.0, WeatherUnits.pressure(1013.25, PressureUnit.MMHG), 0.1)
        assertEquals(1013.25, WeatherUnits.pressure(1013.25, PressureUnit.HPA), 0.001)
    }
}
