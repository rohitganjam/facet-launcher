package com.facetlauncher.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherConditionTest {

    @Test
    fun `sky symbols map to their condition whatever the time of day`() {
        assertEquals(WeatherCondition.CLEAR, conditionOf("clearsky_day"))
        assertEquals(WeatherCondition.CLEAR, conditionOf("clearsky_night"))
        assertEquals(WeatherCondition.MOSTLY_CLEAR, conditionOf("fair_day"))
        assertEquals(WeatherCondition.PARTLY_CLOUDY, conditionOf("partlycloudy_night"))
        assertEquals(WeatherCondition.CLOUDY, conditionOf("cloudy"))
        assertEquals(WeatherCondition.FOG, conditionOf("fog"))
    }

    @Test
    fun `snow, sleet and thunder win over a rain word in the same code`() {
        assertEquals(WeatherCondition.THUNDERSTORM, conditionOf("heavyrainandthunder"))
        assertEquals(WeatherCondition.THUNDERSTORM, conditionOf("rainshowersandthunder_day"))
        assertEquals(WeatherCondition.SNOW, conditionOf("lightsnowshowers_night"))
        assertEquals(WeatherCondition.SLEET, conditionOf("heavysleet"))
    }

    @Test
    fun `without an amount rain follows MET's own name`() {
        assertEquals(WeatherCondition.LIGHT_RAIN, conditionOf("lightrain"))
        assertEquals(WeatherCondition.RAIN, conditionOf("rain"))
        assertEquals(WeatherCondition.HEAVY_RAIN, conditionOf("heavyrainshowers_day"))
        assertEquals(WeatherCondition.RAIN, conditionOf("rainshowers_day"))
    }

    @Test
    fun `an hour's own amount sets how hard it rains`() {
        // MET calls 1.0 mm in an hour "heavyrain"; by the amount it is light rain
        assertEquals(WeatherCondition.LIGHT_RAIN, conditionOf("heavyrain", precipitationMm = 1.0))
        assertEquals(WeatherCondition.RAIN, conditionOf("lightrain", precipitationMm = 3.0))
        assertEquals(WeatherCondition.HEAVY_RAIN, conditionOf("rain", precipitationMm = 9.4))
    }

    @Test
    fun `a trace amount falls back to the symbol's name`() {
        assertEquals(WeatherCondition.LIGHT_RAIN, conditionOf("lightrain", precipitationMm = 0.1))
        assertEquals(WeatherCondition.RAIN, conditionOf("rain", precipitationMm = 0.0))
    }

    @Test
    fun `an amount does not turn a dry symbol into rain`() {
        assertEquals(WeatherCondition.CLOUDY, conditionOf("cloudy", precipitationMm = 5.0))
    }

    @Test
    fun `unknown, empty and missing symbols have no condition`() {
        assertNull(conditionOf("dust"))
        assertNull(conditionOf(""))
        assertNull(conditionOf(null))
    }

    @Test
    fun `only a night suffix is night`() {
        assertTrue(isNightSymbol("clearsky_night"))
        assertFalse(isNightSymbol("clearsky_day"))
        assertFalse(isNightSymbol("clearsky_polartwilight"))
        assertFalse(isNightSymbol("cloudy"))
        assertFalse(isNightSymbol(null))
    }
}
