package com.facetlauncher.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WallpaperOffsetTest {

    @Test
    fun `before Android 14 the middle is half way`() {
        // Given a square wallpaper on a 1080 x 2400 screen on Android 13
        // Then the offset for its middle is 0.5
        assertEquals(0.5f, wallpaperOffsetX(2400, 2400, 1080, 2400, sdk = 33), 0.0001f)
    }

    @Test
    fun `on Android 14 and later a square wallpaper needs an offset past half`() {
        // Given a square wallpaper on a 1080 x 2400 screen on Android 16
        val offset = wallpaperOffsetX(2400, 2400, 1080, 2400, sdk = 36)

        // Then the middle needs about two thirds, since the system's pan range starts at the left edge
        assertEquals(0.609f, offset, 0.01f)
    }

    @Test
    fun `a wallpaper of the screen's own shape needs next to no offset`() {
        // Given a wallpaper the shape of the screen
        val offset = wallpaperOffsetX(1080, 2400, 1080, 2400, sdk = 36)

        // Then there is nothing to pan, so the offset is near the start of the range
        assertTrue(offset in 0f..0.05f)
    }

    @Test
    fun `the offset stays within zero and one`() {
        // Given a very wide wallpaper whose middle is beyond the system's pan range
        val offset = wallpaperOffsetX(9600, 2400, 1080, 2400, sdk = 36)

        // Then it is held at the end of the range
        assertEquals(1f, offset, 0f)
    }
}
