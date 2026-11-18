package com.facetlauncher.app.domain

import android.graphics.RectF
import com.facetlauncher.app.data.model.ScreenSize
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WallpaperRegionTest {

    @Test
    fun `a landscape photo has room for a square around the framing`() {
        // Given a 2000 x 1000 photo framed to a 460 x 1000 portrait strip in the middle
        val region = wallpaperRegion(RectF(770f, 0f, 1230f, 1000f), 2000, 1000)

        // Then the region is the square of the strip's height around its centre
        assertEquals(RectF(500f, 0f, 1500f, 1000f), region)
    }

    @Test
    fun `a framing against an edge has no room and stays as framed`() {
        // Given the same strip framed against the left edge
        val framed = RectF(0f, 0f, 460f, 1000f)

        // Then nothing can be added on its left, so evenly nothing is added on its right
        assertEquals(framed, wallpaperRegion(framed, 2000, 1000))
    }

    @Test
    fun `a framing near an edge is widened as far as the nearer edge allows on both sides`() {
        // Given the strip centred 300 from the left edge of a 2000 x 1000 photo
        val region = wallpaperRegion(RectF(70f, 0f, 530f, 1000f), 2000, 1000)

        // Then it is widened evenly to 300 each side, short of the square it would otherwise be
        assertEquals(RectF(0f, 0f, 600f, 1000f), region)
    }

    @Test
    fun `a wide framing is widened upwards and downwards`() {
        // Given a landscape screen's framing, 1000 x 460, in the middle of a 1000 x 1000 photo
        val region = wallpaperRegion(RectF(0f, 270f, 1000f, 730f), 1000, 1000)

        // Then it grows to the square of its width
        assertEquals(RectF(0f, 0f, 1000f, 1000f), region)
    }

    @Test
    fun `a portrait photo framed edge to edge has no room and stays as framed`() {
        // Given a 1080 x 2340 photo framed to its full width
        val framed = RectF(0f, 0f, 1080f, 2340f)

        // Then it is used as it is
        assertEquals(framed, wallpaperRegion(framed, 1080, 2340))
    }

    @Test
    fun `a square region is cut at the longest side and a narrower one in proportion`() {
        // Given two screens
        val screens = listOf(ScreenSize(1080, 2400), ScreenSize(1080, 2092))

        // Then a square is as big as the longest side, and a narrower region has its longer side that long
        assertEquals(2400 to 2400, wallpaperSize(RectF(0f, 0f, 1000f, 1000f), screens))
        assertEquals(1104 to 2400, wallpaperSize(RectF(0f, 0f, 460f, 1000f), screens))
        assertEquals(1800 to 2400, wallpaperSize(RectF(0f, 0f, 600f, 800f), screens))
    }
}
