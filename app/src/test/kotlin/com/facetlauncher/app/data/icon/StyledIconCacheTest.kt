package com.facetlauncher.app.data.icon

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StyledIconCacheTest {
    private fun logo(): Bitmap {
        val side = 64
        val pixels = IntArray(side * side) { 0xFF3366CC.toInt() }
        for (y in 20 until 44) for (x in 20 until 44) pixels[y * side + x] = 0xFFFFFFFF.toInt()
        return Bitmap.createBitmap(pixels, side, side, Bitmap.Config.ARGB_8888)
    }

    @Test
    fun `styles an icon into stored size glyph and outline pictures`() = runTest {
        // When styling a logo
        val styled = StyledIconCache().styled(logo().asImageBitmap(), themedCoverage = null)

        // Then both pictures exist at the stored size
        assertEquals(STORED_ICON_SIZE, styled.glyph!!.asAndroidBitmap().width)
        assertNotNull(styled.outline)
    }

    @Test
    fun `styling the same icon twice reuses the first result`() = runTest {
        // Given an icon already styled
        val cache = StyledIconCache()
        val icon = logo().asImageBitmap()
        val first = cache.styled(icon, themedCoverage = null)

        // When styling it again
        val second = cache.styled(icon, themedCoverage = null)

        // Then the cached result comes back
        assertSame(first, second)
    }
}
