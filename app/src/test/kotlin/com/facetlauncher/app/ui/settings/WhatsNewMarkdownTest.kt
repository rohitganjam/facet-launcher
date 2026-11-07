package com.facetlauncher.app.ui.settings

import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WhatsNewMarkdownTest {

    @Test
    fun `bold runs lose their markers and are drawn bold`() {
        // Given a bullet with one bold run in the middle
        val result = boldMarkdown("Search shows **settings and contacts** as tiles")

        // Then the markers are gone and exactly that run is bold
        assertEquals("Search shows settings and contacts as tiles", result.text)
        val span = result.spanStyles.single()
        assertEquals(FontWeight.Bold, span.item.fontWeight)
        assertEquals("settings and contacts", result.text.substring(span.start, span.end))
    }

    @Test
    fun `several bold runs are each drawn bold`() {
        val result = boldMarkdown("**One** and **two**")

        assertEquals("One and two", result.text)
        assertEquals(listOf("One", "two"), result.spanStyles.map { result.text.substring(it.start, it.end) })
    }

    @Test
    fun `a bullet without markers is unchanged`() {
        val result = boldMarkdown("Plain text with a * star")

        assertEquals("Plain text with a * star", result.text)
        assertTrue(result.spanStyles.isEmpty())
    }

    @Test
    fun `an unclosed marker stays as typed`() {
        val result = boldMarkdown("Costs **extra")

        assertEquals("Costs **extra", result.text)
        assertTrue(result.spanStyles.isEmpty())
    }

    @Test
    fun `an empty pair stays as typed`() {
        val result = boldMarkdown("Before **** after")

        assertEquals("Before **** after", result.text)
        assertTrue(result.spanStyles.isEmpty())
    }

    @Test
    fun `bold at the start and end of the line works`() {
        val result = boldMarkdown("**Start** middle **end**")

        assertEquals("Start middle end", result.text)
        assertEquals(listOf("Start", "end"), result.spanStyles.map { result.text.substring(it.start, it.end) })
    }
}
