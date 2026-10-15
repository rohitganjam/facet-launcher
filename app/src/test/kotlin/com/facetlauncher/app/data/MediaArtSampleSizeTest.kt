package com.facetlauncher.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaArtSampleSizeTest {
    @Test
    fun `art at or below the cap is not downsampled`() {
        // Given / When / Then
        assertEquals(1, artSampleSize(512, 512))
        assertEquals(1, artSampleSize(300, 300))
    }

    @Test
    fun `large art is halved until the next halving would drop below the cap`() {
        // Given / When / Then
        assertEquals(2, artSampleSize(1024, 1024))
        assertEquals(8, artSampleSize(4096, 4096))
    }

    @Test
    fun `the shorter edge decides the sample size`() {
        // Given / When / Then
        assertEquals(1, artSampleSize(4096, 600))
    }

    @Test
    fun `unreadable bounds fall back to no downsampling`() {
        // Given / When / Then
        assertEquals(1, artSampleSize(-1, -1))
        assertEquals(1, artSampleSize(0, 0))
    }
}
