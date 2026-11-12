package com.facetlauncher.app.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WallpaperFitTest {

    // A landscape photo 4000 x 3000 seen through a portrait window 1000 x 2000 (the screen's shape).
    private val geometry = FitGeometry(imageWidth = 4000, imageHeight = 3000, boxWidth = 1000f, boxHeight = 2000f)

    @Test
    fun `fully zoomed out the window just covers the photo and is centred`() {
        // When nothing has been moved
        val visible = WallpaperFit.initial(geometry).visible(geometry)

        // Then the whole height shows (the photo is wider than the screen) and it is centred sideways
        assertEquals(0f, visible.top, 0.01f)
        assertEquals(3000f, visible.height(), 0.01f)
        assertEquals(1500f, visible.width(), 0.01f)
        assertEquals(2000f, visible.centerX(), 0.01f)
    }

    @Test
    fun `the visible part always has the window's shape`() {
        // Given a fit that was zoomed and dragged
        val fit = WallpaperFit.initial(geometry).zoomed(2.3f, 300f, 700f, geometry).panned(-120f, 90f, geometry)

        // Then the part in view keeps the window's aspect
        val visible = fit.visible(geometry)
        assertEquals(geometry.boxWidth / geometry.boxHeight, visible.width() / visible.height(), 0.001f)
    }

    @Test
    fun `dragging past the photo's edge stops at the edge`() {
        // When dragged a very long way right (the photo follows, so we look further left)
        val far = WallpaperFit.initial(geometry).panned(100000f, 0f, geometry).visible(geometry)

        // Then the window rests on the photo's left edge
        assertEquals(0f, far.left, 0.01f)
        assertTrue(far.right <= geometry.imageWidth)
    }

    @Test
    fun `zooming in shrinks the visible part and is capped`() {
        // When zoomed in twice as far
        val zoomed = WallpaperFit.initial(geometry).zoomed(2f, 500f, 1000f, geometry)

        // Then half as much is visible
        assertEquals(750f, zoomed.visible(geometry).width(), 0.01f)

        // And zooming absurdly far stops at the maximum
        val max = WallpaperFit.initial(geometry).zoomed(1000f, 500f, 1000f, geometry)
        assertEquals(MAX_FIT_ZOOM, max.zoom, 0.0001f)
    }

    @Test
    fun `zooming out never goes below covering the photo`() {
        // When zoomed out from the start
        val out = WallpaperFit.initial(geometry).zoomed(0.1f, 500f, 1000f, geometry)

        // Then zoom stays at 1
        assertEquals(1f, out.zoom, 0.0001f)
    }

    @Test
    fun `the photo point under the pinch stays under it`() {
        // Given a fit and a pinch focus
        val start = WallpaperFit.initial(geometry).zoomed(1.5f, 100f, 100f, geometry)
        val focusX = 600f
        val focusY = 1200f
        fun photoPointUnder(fit: WallpaperFit): Pair<Float, Float> {
            val v = fit.visible(geometry)
            val scale = geometry.coverScale * fit.zoom
            return v.left + focusX / scale to v.top + focusY / scale
        }
        val before = photoPointUnder(start)

        // When zoomed in about that point
        val after = photoPointUnder(start.zoomed(1.6f, focusX, focusY, geometry))

        // Then the same photo point is still under it
        assertEquals(before.first, after.first, 0.5f)
        assertEquals(before.second, after.second, 0.5f)
    }

    @Test
    fun `a photo already the window's shape shows whole and does not throw`() {
        // Given a photo whose aspect matches the window to within float rounding
        val matching = FitGeometry(imageWidth = 1080, imageHeight = 2400, boxWidth = 411f, boxHeight = 913.3334f)

        // When it is fully zoomed out
        val visible = WallpaperFit.initial(matching).visible(matching)

        // Then the visible part sits at the photo's corner and covers it
        assertEquals(0f, visible.left, 0.5f)
        assertEquals(0f, visible.top, 0.5f)
        assertEquals(1080f, visible.width(), 1f)
    }
}
