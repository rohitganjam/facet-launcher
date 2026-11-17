package com.facetlauncher.app.data

import android.graphics.Bitmap
import android.graphics.Color
import java.nio.ByteBuffer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScreenCaptureFrameTest {

    /** A frame of [width] x [height] pixels, [rowStride] bytes a row, each pixel [pixel] (RGBA bytes) and the padding zeros. */
    private fun frame(width: Int, height: Int, rowStride: Int, pixel: Int): ByteBuffer {
        val buffer = ByteBuffer.allocate(rowStride * height)
        repeat(height) { row ->
            repeat(width) { column ->
                val at = row * rowStride + column * 4
                buffer.put(at, Color.red(pixel).toByte())
                buffer.put(at + 1, Color.green(pixel).toByte())
                buffer.put(at + 2, Color.blue(pixel).toByte())
                buffer.put(at + 3, Color.alpha(pixel).toByte())
            }
        }
        return buffer
    }

    @Test
    fun `a frame with no row padding keeps its size and pixels`() {
        // Given a 4 x 3 frame whose rows are exactly 16 bytes
        val pixels = frame(width = 4, height = 3, rowStride = 16, pixel = Color.WHITE)

        // When it becomes a bitmap
        val bitmap = frameToBitmap(pixels, width = 4, height = 3, rowStride = 16, pixelStride = 4)

        // Then it is 4 x 3 and white
        assertEquals(4, bitmap.width)
        assertEquals(3, bitmap.height)
        assertEquals(Color.WHITE, bitmap.getPixel(3, 2))
    }

    @Test
    fun `padding at the end of each row is cut off`() {
        // Given a 4 x 3 frame whose rows are padded to 24 bytes
        val pixels = frame(width = 4, height = 3, rowStride = 24, pixel = Color.WHITE)

        // When it becomes a bitmap
        val bitmap = frameToBitmap(pixels, width = 4, height = 3, rowStride = 24, pixelStride = 4)

        // Then only the 4 real pixels of each row remain
        assertEquals(4, bitmap.width)
        assertEquals(3, bitmap.height)
        assertEquals(Color.WHITE, bitmap.getPixel(3, 2))
    }

    /** A 2-wide bitmap whose row `y` is the colour with red = y, so a row's origin can be read back from its pixel. */
    private fun numberedRows(height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(2, height, Bitmap.Config.ARGB_8888)
        repeat(height) { y -> repeat(2) { x -> bitmap.setPixel(x, y, Color.rgb(y, 0, 0)) } }
        return bitmap
    }

    @Test
    fun `the status bar rows are replaced by the rows below mirrored`() {
        // Given a 10-row frame with a 2-row status bar
        val frame = numberedRows(10)

        // When the bars are filled
        val filled = frame.withBarsFilled(top = 2, bottom = 0)

        // Then rows 0 and 1 hold what rows 3 and 2 held, and the rest is unchanged
        assertEquals(3, Color.red(filled.getPixel(0, 0)))
        assertEquals(2, Color.red(filled.getPixel(1, 1)))
        assertEquals(5, Color.red(filled.getPixel(0, 5)))
    }

    @Test
    fun `the navigation bar rows are replaced by the rows above mirrored`() {
        // Given a 10-row frame with a 2-row navigation bar
        val frame = numberedRows(10)

        // When the bars are filled
        val filled = frame.withBarsFilled(top = 0, bottom = 2)

        // Then rows 8 and 9 hold what rows 7 and 6 held
        assertEquals(7, Color.red(filled.getPixel(0, 8)))
        assertEquals(6, Color.red(filled.getPixel(1, 9)))
        assertEquals(4, Color.red(filled.getPixel(0, 4)))
    }

    @Test
    fun `no bars leaves the frame as it was`() {
        // Given a frame and no bar heights
        val frame = numberedRows(10)

        // When the bars are filled
        val filled = frame.withBarsFilled(top = 0, bottom = 0)

        // Then it is the same frame
        assertEquals(frame, filled)
    }

    @Test
    fun `bars covering half the frame or more leave it alone`() {
        // Given bars that would cover most of a 10-row frame
        val frame = numberedRows(10)

        // When the bars are filled
        val filled = frame.withBarsFilled(top = 3, bottom = 3)

        // Then nothing is painted over
        assertEquals(0, Color.red(filled.getPixel(0, 0)))
    }
}
