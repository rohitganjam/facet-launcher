package com.facetlauncher.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AssignCalendarColorsUseCaseTest {

    private val useCase = AssignCalendarColorsUseCase()
    private val palette = listOf("BLUE", "INDIGO", "PURPLE")

    @Test
    fun `assigns a distinct palette entry to each new calendar`() {
        val result = useCase(calendarIds = listOf("1", "2"), existingColors = emptyMap(), palette = palette)

        assertEquals("BLUE", result["1"])
        assertEquals("INDIGO", result["2"])
    }

    @Test
    fun `leaves an already-colored calendar untouched`() {
        val existing = mapOf("1" to "PURPLE")

        val result = useCase(calendarIds = listOf("1", "2"), existingColors = existing, palette = palette)

        assertEquals("PURPLE", result["1"])
        assertEquals("BLUE", result["2"])
    }

    @Test
    fun `cycles back through the palette once every entry is already in use`() {
        val existing = mapOf("1" to "BLUE", "2" to "INDIGO", "3" to "PURPLE")

        val result = useCase(calendarIds = listOf("1", "2", "3", "4"), existingColors = existing, palette = palette)

        assertEquals("BLUE", result["4"])
    }

    @Test
    fun `an empty palette leaves existing colors unchanged`() {
        val existing = mapOf("1" to "BLUE")

        val result = useCase(calendarIds = listOf("1", "2"), existingColors = existing, palette = emptyList())

        assertEquals(existing, result)
    }

    @Test
    fun `re-running with the same inputs is stable`() {
        val first = useCase(calendarIds = listOf("1", "2", "3"), existingColors = emptyMap(), palette = palette)
        val second = useCase(calendarIds = listOf("1", "2", "3"), existingColors = first, palette = palette)

        assertEquals(first, second)
    }
}
