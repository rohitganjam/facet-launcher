package com.facetlauncher.app.data.icon

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.facetlauncher.app.data.model.AppInfo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    fun `styles an icon into a stored size glyph and keeps no outline`() = runTest {
        // When styling a logo as a glyph
        val styled = StyledIconCache().styled(logo().asImageBitmap(), themedCoverage = null, StyledPicture.GLYPH)

        // Then the glyph exists at the stored size and the outline is left out
        assertEquals(STORED_ICON_SIZE, styled.glyph!!.asAndroidBitmap().width)
        assertNull(styled.outline)
    }

    @Test
    fun `styles an icon into an outline and keeps no glyph`() = runTest {
        // When styling a logo as an outline
        val styled = StyledIconCache().styled(logo().asImageBitmap(), themedCoverage = null, StyledPicture.OUTLINE)

        // Then the outline exists and the glyph is left out
        assertNotNull(styled.outline)
        assertNull(styled.glyph)
    }

    @Test
    fun `styling the same icon twice reuses the first result`() = runTest {
        // Given an icon already styled
        val cache = StyledIconCache()
        val icon = logo().asImageBitmap()
        val first = cache.styled(icon, themedCoverage = null, StyledPicture.GLYPH)

        // When styling it again
        val second = cache.styled(icon, themedCoverage = null, StyledPicture.GLYPH)

        // Then the cached result comes back
        assertSame(first, second)
    }

    @Test
    fun `asking for the other picture replaces the one kept`() = runTest {
        // Given an icon styled as a glyph
        val cache = StyledIconCache()
        val icon = logo().asImageBitmap()
        cache.styled(icon, themedCoverage = null, StyledPicture.GLYPH)

        // When styling it as an outline
        cache.styled(icon, themedCoverage = null, StyledPicture.OUTLINE)

        // Then only the outline is kept
        assertNull(cache.cached(icon, StyledPicture.GLYPH))
        assertNotNull(cache.cached(icon, StyledPicture.OUTLINE))
    }

    @Test
    fun `an icon already styled can be looked up at once and one not yet styled cannot`() = runTest {
        // Given one icon styled and another not
        val cache = StyledIconCache()
        val styledIcon = logo().asImageBitmap()
        val other = logo().asImageBitmap()
        val styled = cache.styled(styledIcon, themedCoverage = null, StyledPicture.GLYPH)

        // Then only the styled one is found without waiting
        assertSame(styled, cache.cached(styledIcon, StyledPicture.GLYPH))
        assertNull(cache.cached(other, StyledPicture.GLYPH))
    }

    @Test
    fun `warming styles every app that has an icon and skips the rest`() = runTest {
        // Given two apps with icons and one without
        val cache = StyledIconCache()
        val first = logo().asImageBitmap()
        val second = logo().asImageBitmap()
        val apps = listOf(
            AppInfo(packageName = "a", activityName = ".A", label = "A", icon = first),
            AppInfo(packageName = "b", activityName = ".B", label = "B", icon = second),
            AppInfo(packageName = "c", activityName = ".C", label = "C", icon = null),
        )

        // When warming
        cache.warm(apps, StyledPicture.OUTLINE)

        // Then both icons are ready without further waiting
        assertNotNull(cache.cached(first, StyledPicture.OUTLINE))
        assertNotNull(cache.cached(second, StyledPicture.OUTLINE))
    }

    @Test
    fun `clearing forgets every picture`() = runTest {
        // Given an icon styled
        val cache = StyledIconCache()
        val icon = logo().asImageBitmap()
        cache.styled(icon, themedCoverage = null, StyledPicture.GLYPH)

        // When clearing
        cache.clear()

        // Then nothing is kept
        assertNull(cache.cached(icon, StyledPicture.GLYPH))
    }
}
