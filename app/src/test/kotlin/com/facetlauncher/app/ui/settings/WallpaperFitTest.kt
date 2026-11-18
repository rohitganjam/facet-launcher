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

    // The same, for a wallpaper the system will magnify by 1.1 about its middle once set.
    private val zoomed = FitGeometry(imageWidth = 4000, imageHeight = 3000, boxWidth = 1000f, boxHeight = 2000f, systemZoom = 1.1f)

    @Test
    fun `with the system's zoom the window starts zoomed in by the same amount`() {
        // When nothing has been moved
        val fit = WallpaperFit.initial(zoomed)

        // Then it is 1.1 times closer than the part that just covers the photo, and still centred
        assertEquals(1.1f, fit.zoom, 0.0001f)
        assertEquals(2000f, fit.visible(zoomed).centerX(), 0.01f)
    }

    @Test
    fun `pinching out stops at the system's zoom`() {
        // When pinched far out
        val fit = WallpaperFit.initial(zoomed).zoomed(0.1f, 500f, 1000f, zoomed)

        // Then it does not go further out than the system's zoom
        assertEquals(1.1f, fit.zoom, 0.0001f)
    }

    @Test
    fun `the part the system hides stays inside the photo wherever the window is dragged`() {
        // Given a window dragged a very long way to each side
        val left = WallpaperFit.initial(zoomed).panned(100000f, 0f, zoomed)
        val right = WallpaperFit.initial(zoomed).panned(-100000f, 0f, zoomed)

        // Then the larger part to set, which the system will magnify back to the window, is inside the photo on both sides
        assertTrue(left.wallpaperPart(zoomed).left >= -0.01f)
        assertTrue(right.wallpaperPart(zoomed).right <= 4000.01f)
    }

    @Test
    fun `the part to set is the window grown by the system's zoom about its centre`() {
        // Given a zoomed-in window
        val fit = WallpaperFit.initial(zoomed).zoomed(2f, 500f, 1000f, zoomed)
        val shown = fit.visible(zoomed)

        // Then the part to set has the same centre and is 1.1 times larger each way
        val part = fit.wallpaperPart(zoomed)
        assertEquals(shown.centerX(), part.centerX(), 0.01f)
        assertEquals(shown.centerY(), part.centerY(), 0.01f)
        assertEquals(shown.width() * 1.1f, part.width(), 0.01f)
        assertEquals(shown.height() * 1.1f, part.height(), 0.01f)
    }

    @Test
    fun `without the system's zoom the part to set is the window itself`() {
        // Given a screenshot, which is already as shown
        val fit = WallpaperFit.initial(geometry).zoomed(2f, 500f, 1000f, geometry)

        // Then the part to set is what is in the window
        assertEquals(fit.visible(geometry), fit.wallpaperPart(geometry))
    }

    @Test
    fun `a scheduled daily photo is set as the framing step starts, so the two show the same`() {
        // Given a wide photo and a portrait screen, framed untouched and cut the way the scheduled daily wallpaper cuts it
        val geometry = FitGeometry(imageWidth = 4000, imageHeight = 2000, boxWidth = 1000f, boxHeight = 2000f, systemZoom = 1.1f)
        val framed = WallpaperFit.initial(geometry).wallpaperPart(geometry)
        val strip = com.facetlauncher.app.domain.portraitCentreCrop(4000, 2000, 1000, 2000)!!.region

        // Then the part set is the same centre strip, so the system's zoom leaves the same view in both
        assertEquals(strip.left.toFloat(), framed.left, 0.5f)
        assertEquals(strip.right.toFloat(), framed.right, 0.5f)
        assertEquals(strip.top.toFloat(), framed.top, 0.5f)
        assertEquals(strip.bottom.toFloat(), framed.bottom, 0.5f)
    }
}
