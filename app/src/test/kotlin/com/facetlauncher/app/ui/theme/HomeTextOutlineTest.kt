package com.facetlauncher.app.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeTextOutlineTest {

    @Test
    fun `black and dark grey text get the light outline`() {
        // Given dark, uncoloured text colors (the dark ink and a dark muted grey)
        val ink = Color(0xFF020817)
        val darkGrey = Color(0xFF5B6472)

        // Then both are outlined
        assertTrue(homeTextOutlined(ink))
        assertTrue(homeTextOutlined(darkGrey))
    }

    @Test
    fun `white and light grey text get no outline`() {
        // Given light text colors
        val white = Color(0xFFE7EAF0)
        val lightGrey = Color(0xFFB4BAC6)

        // Then neither is outlined
        assertFalse(homeTextOutlined(white))
        assertFalse(homeTextOutlined(lightGrey))
    }

    @Test
    fun `accent colors get no outline however dark they are`() {
        // Given saturated colors, dark and mid
        val blue = Color(0xFF2563EB)
        val navy = Color(0xFF1E3A8A)
        val maroon = Color(0xFF7F1D1D)
        val purple = Color(0xFF7C3AED)

        // Then none is outlined, so no accent ever gets a light edge
        listOf(blue, navy, maroon, purple).forEach { assertFalse(it.toString(), homeTextOutlined(it)) }
    }
}
