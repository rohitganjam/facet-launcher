package com.facetlauncher.app.domain

import com.facetlauncher.app.data.clockItemWith
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NativeWidgetType
import org.junit.Assert.assertEquals
import org.junit.Test

class ClockConfigResolutionTest {

    private val settings = LauncherSettings()

    @Test
    fun `a clock's own settings are what is in force`() {
        // Given a clock with settings of its own
        val own = ClockWidgetConfig(templateId = ClockTemplateId.entries.last(), use24HourTime = true, showWeather = true)
        val facet = Facet(name = "Work", position = 0, clockStack = listOf(clockItemWith(own)))

        // Then they are what the facet shows
        assertEquals(own, effectiveClockConfig(facet, settings))
    }

    @Test
    fun `a clock with nothing stored yet shows the built-in defaults`() {
        // Given a facet whose clock page has no settings
        val facet = Facet(name = "Home", position = 0)

        // Then the defaults are in force
        assertEquals(ClockWidgetConfig(), effectiveClockConfig(facet, settings))
    }

    @Test
    fun `a facet with no clock page and no facet at all show the defaults`() {
        // Given a facet whose stack holds only a widget
        val facet = Facet(name = "Home", position = 0, clockStack = listOf(ClockStackItem(id = 3, facetId = 1, position = 0, kind = ClockStackItemKind.NATIVE_WIDGET, nativeWidgetType = NativeWidgetType.WEATHER)))

        // Then the defaults are in force, as they are with no facet
        assertEquals(ClockWidgetConfig(), effectiveClockConfig(facet, settings))
        assertEquals(ClockWidgetConfig(), effectiveClockConfig(null, settings))
    }
}
