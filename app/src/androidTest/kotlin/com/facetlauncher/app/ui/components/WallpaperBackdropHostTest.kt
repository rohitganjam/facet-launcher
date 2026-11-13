package com.facetlauncher.app.ui.components

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** The wallpaper copy is only sampled by glass: the system wallpaper (here a blue background) stays what shows. */
class WallpaperBackdropHostTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val copy = Bitmap.createBitmap(40, 80, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.Red.toArgb()) }

    private fun setHost() {
        // Below API 31 there is no backdrop at all.
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        composeRule.setContent {
            Box(Modifier.fillMaxSize().background(Color.Blue).testTag("screen")) {
                WallpaperBackdropHost(copy) {
                    val backdrop = LocalWallpaperBackdrop.current
                    var coordinates: LayoutCoordinates? = null
                    // Stands in for a glass panel: draws whatever the backdrop holds behind it.
                    Box(
                        Modifier
                            .align(Alignment.Center)
                            .size(48.dp)
                            .testTag("panel")
                            .onGloballyPositioned { coordinates = it }
                            .drawBehind { backdrop?.run { drawBackdrop(this@drawBehind, coordinates, null) } },
                    )
                }
            }
        }
    }

    @Test
    fun `the copy is not drawn over the system wallpaper`() {
        // Given a red copy over a blue wallpaper
        setHost()

        // Then the screen outside any panel stays blue
        val pixels = composeRule.onNodeWithTag("screen").captureToImage().toPixelMap()
        assertEquals(Color.Blue.toArgb(), pixels[2, 2].toArgb())
    }

    @Test
    fun `a glass panel still samples the copy`() {
        // Given a red copy and a panel drawing the backdrop
        setHost()

        // Then the panel shows the copy
        val pixels = composeRule.onNodeWithTag("panel").captureToImage().toPixelMap()
        assertEquals(Color.Red.toArgb(), pixels[pixels.width / 2, pixels.height / 2].toArgb())
    }
}
