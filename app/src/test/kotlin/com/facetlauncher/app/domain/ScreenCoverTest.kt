package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.ScreenSize
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScreenCoverTest {

    private val inner = ScreenSize(2208, 1840)
    private val outer = ScreenSize(1080, 2092)

    @Test
    fun `a screen shaped like the image shows all of it`() {
        // When the inner screen covers an image shaped like itself
        val region = inner.coverRegion(2208, 1840)

        // Then the whole image shows
        assertEquals(0f, region.left, 0.01f)
        assertEquals(0f, region.top, 0.01f)
        assertEquals(2208f, region.right, 0.01f)
        assertEquals(1840f, region.bottom, 0.01f)
    }

    @Test
    fun `a taller screen shows a full height strip from the middle`() {
        // When the outer screen covers the inner-shaped image
        val region = outer.coverRegion(2208, 1840)

        // Then it shows the full height and a centred strip of the width, as the Fold did
        assertEquals(1840f, region.height(), 0.5f)
        assertEquals(950f, region.width(), 1f)
        assertEquals(1104f, region.centerX(), 0.5f)
    }

    @Test
    fun `a wider screen shows a full width band from the middle`() {
        // When the inner screen covers a tall image
        val region = inner.coverRegion(1000, 2000)

        // Then it shows the full width and a centred band of the height
        assertEquals(1000f, region.width(), 0.5f)
        assertEquals(1000f * 1840 / 2208, region.height(), 1f)
        assertEquals(1000f, region.centerY(), 0.5f)
    }

    @Test
    fun `the region never leaves the image`() {
        // When covering an image of any shape
        for ((w, h) in listOf(100 to 100, 4000 to 300, 300 to 4000)) {
            val region = outer.coverRegion(w, h)

            // Then it is inside it
            assertEquals(true, region.left >= -0.01f && region.top >= -0.01f && region.right <= w + 0.01f && region.bottom <= h + 0.01f)
        }
    }
}
