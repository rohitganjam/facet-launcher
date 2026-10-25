package com.facetlauncher.app.ui.home.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherIconColorsTest {

    @Test
    fun `every weather picture has its own coloured drawable`() {
        // Given every kind of weather picture
        val resources = WeatherIconKind.entries.map { it.coloredRes() }

        // Then each maps to a real resource, none shared
        assertTrue(resources.all { it != 0 })
        assertEquals(resources.size, resources.toSet().size)
    }
}
