package com.facetlauncher.app.ui.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Surface
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OrphanedWidgetTileTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun theTileHasASixtyPercentBackgroundSoItReadsOverABusyWallpaper() {
        // Given the tile over pure red, standing in for a loud wallpaper
        var surface = Color.Unspecified
        composeRule.setContent {
            FacetLauncherTheme {
                surface = Surface
                Box(modifier = Modifier.size(220.dp, 176.dp).background(Color.Red)) {
                    OrphanedWidgetTile(providerLabel = "Ledger", onRemove = {}, onKeepSpace = null)
                }
            }
        }

        // When a pixel in its middle (clear of the text) is read back
        val pixels = composeRule.onNodeWithTag("hub_orphaned_widget").captureToImage().toPixelMap()
        val pixel = pixels[pixels.width / 2, pixels.height / 2]

        // Then it is the surface at 60% over the red: the green channel (red has none) is 60% of the surface's, and the red is lifted by the 40% left
        assertEquals(0.6f * surface.green, pixel.green, 0.05f)
        assertEquals(0.6f * surface.red + 0.4f, pixel.red, 0.05f)
    }
}
