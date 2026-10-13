package com.facetlauncher.app.ui.launcher

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeLayerFadeTest {

    @Test
    fun `home is untouched while every panel is closed`() {
        assertEquals(HomeLayerFade(alpha = 1f, scale = 1f), homeLayerFade(0f, 0f, 0f))
    }

    @Test
    fun `an open drawer hub or carousel fades home out and shrinks it the same way`() {
        val open = HomeLayerFade(alpha = 0f, scale = 0.97f)

        assertEquals(open, homeLayerFade(1f, 0f, 0f))
        assertEquals(open, homeLayerFade(0f, 1f, 0f))
        assertEquals(open, homeLayerFade(0f, 0f, 1f))
    }

    @Test
    fun `the furthest open panel drives the fade`() {
        // Given the hub half open while the carousel is only a quarter open
        val fade = homeLayerFade(0f, 0.5f, 0.25f)

        // Then half the fade applies
        assertEquals(0.5f, fade.alpha, 0.0001f)
        assertEquals(1f - 0.5f * 0.03f, fade.scale, 0.0001f)
    }
}
