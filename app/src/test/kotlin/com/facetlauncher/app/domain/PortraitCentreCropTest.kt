package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.ImageRegion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PortraitCentreCropTest {

    @Test
    fun `a uhd landscape photo keeps its centre strip at full height on a 20 by 9 phone`() {
        // Given a 3840 x 2160 photo and a 1080 x 2400 screen

        // When cropped
        val crop = portraitCentreCrop(3840, 2160, 1080, 2400)!!

        // Then the strip is 972 x 2160, centred, decoded at full size
        assertEquals(ImageRegion(1434, 0, 2406, 2160), crop.region)
        assertEquals(1, crop.sampleSize)
    }

    @Test
    fun `a 1080p landscape photo is too small for a sharp portrait wallpaper`() {
        // Given a 1920 x 1080 photo, whose strip would be 486 px wide on a 1080 px wide screen

        // When cropped
        val crop = portraitCentreCrop(1920, 1080, 1080, 2400)

        // Then it is refused
        assertNull(crop)
    }

    @Test
    fun `a slightly small strip is still accepted`() {
        // Given a 3840 x 2000 photo: its strip is 900 px wide, enlarged 1_2 times
        val crop = portraitCentreCrop(3840, 2000, 1080, 2400)

        // Then it is used
        assertEquals(900, crop!!.region.width)
    }

    @Test
    fun `a strip is never sampled below the screen's size`() {
        // Given an 8000 x 4500 photo and a 1080 x 2400 screen
        val crop = portraitCentreCrop(8000, 4500, 1080, 2400)!!

        // Then the 2025 x 4500 strip is decoded whole, since half (1012 x 2250) would be smaller than the screen
        assertEquals(2025, crop.region.width)
        assertEquals(1, crop.sampleSize)
    }

    @Test
    fun `a photo four times the screen is decoded at a quarter`() {
        // Given a strip exactly four times the screen
        val crop = portraitCentreCrop(8000, 8000, 1000, 2000)!!

        // Then it is sampled by 4
        assertEquals(ImageRegion(2000, 0, 6000, 8000), crop.region)
        assertEquals(4, crop.sampleSize)
    }

    @Test
    fun `a portrait photo taller than the screen keeps its full width and the vertical centre`() {
        // Given a 1200 x 3000 photo and a 1080 x 2400 screen (0_45 wide)
        val crop = portraitCentreCrop(1200, 3000, 1080, 2400)!!

        // Then the full width is kept and the height cut to 2666, centred
        assertEquals(ImageRegion(0, 167, 1200, 2833), crop.region)
    }

    @Test
    fun `no size gives no crop`() {
        assertNull(portraitCentreCrop(0, 100, 1080, 2400))
        assertNull(portraitCentreCrop(100, 100, 0, 2400))
    }
}
