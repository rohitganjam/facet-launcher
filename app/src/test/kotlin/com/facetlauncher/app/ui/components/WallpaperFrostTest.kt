package com.facetlauncher.app.ui.components

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
class WallpaperFrostTest {

    private fun solid(width: Int, height: Int, colour: Int) = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(colour) }

    private fun checkerboard(size: Int, cell: Int) = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
        for (y in 0 until size) for (x in 0 until size) setPixel(x, y, if ((x / cell + y / cell) % 2 == 0) Color.WHITE else Color.BLACK)
    }

    private fun spread(bitmap: Bitmap): Int {
        val reds = (0 until bitmap.width).flatMap { x -> (0 until bitmap.height).map { y -> Color.red(bitmap.getPixel(x, y)) } }
        return reds.max() - reds.min()
    }

    @Test
    fun `a large copy is reduced so the blur stays cheap`() {
        // Given a full-size copy
        val copy = solid(1080, 2340, Color.GRAY)

        // When it is frosted
        val frost = frostedCopy(copy, blurPx = 48f, brightness = 0f, saturation = 1f)

        // Then its long side is at most 320
        assertTrue(maxOf(frost.width, frost.height) <= 320)
    }

    @Test
    fun `a flat colour stays the same colour`() {
        // Given a flat copy
        val frost = frostedCopy(solid(200, 400, Color.rgb(90, 120, 200)), blurPx = 10f, brightness = 0f, saturation = 1f)

        // Then every pixel is that colour, edges included
        assertEquals(Color.rgb(90, 120, 200), frost.getPixel(0, 0))
        assertEquals(Color.rgb(90, 120, 200), frost.getPixel(frost.width / 2, frost.height / 2))
        assertEquals(Color.rgb(90, 120, 200), frost.getPixel(frost.width - 1, frost.height - 1))
    }

    @Test
    fun `detail is blurred away`() {
        // Given a sharp checkerboard
        val copy = checkerboard(256, cell = 4)
        assertEquals(255, spread(copy))

        // When it is frosted
        val frost = frostedCopy(copy, blurPx = 12f, brightness = 0f, saturation = 1f)

        // Then the light and dark squares have run together
        assertTrue(spread(frost) < 40)
    }

    @Test
    fun `brightness lifts and darkens the copy`() {
        // Given a mid grey
        val grey = solid(64, 64, Color.rgb(100, 100, 100))

        // Then a positive brightness lifts it and a negative one lowers it
        assertTrue(Color.red(frostedCopy(grey, 4f, 0.1f, 1f).getPixel(10, 10)) > 100)
        assertTrue(Color.red(frostedCopy(grey, 4f, -0.1f, 1f).getPixel(10, 10)) < 100)
    }

    @Test
    fun `saturation changes how vivid the copy is`() {
        // Given a colourful copy
        val red = solid(64, 64, Color.rgb(200, 60, 60))

        // When saturation is removed the channels meet, and when raised they move apart
        val grey = frostedCopy(red, 4f, 0f, 0f).getPixel(10, 10)
        val vivid = frostedCopy(red, 4f, 0f, 1.5f).getPixel(10, 10)

        // Then
        assertTrue(abs(Color.red(grey) - Color.green(grey)) <= 1)
        assertTrue(Color.red(vivid) - Color.green(vivid) > 140)
    }
}
