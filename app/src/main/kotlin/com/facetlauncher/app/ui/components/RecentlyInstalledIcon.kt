package com.facetlauncher.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.InkInverted

/** The pixel size the icon's two layers are drawn at; [AppIcon] scales them to the size asked for. */
private const val ICON_PX = 192

/** How much of the icon the glyph fills: on the tile it is a folder tile's mini-icon proportion, in the themed layer it fills an adaptive icon's safe zone. */
private val GLYPH_ON_TILE = AppIconSize.FOLDER_MINI.value / AppIconSize.ICON.value
private const val GLYPH_THEMED = 0.5f

private class RecencyLayers(val tile: ImageBitmap, val themed: ImageBitmap)

/**
 * The "Recently installed" category's icon, drawn by [AppIcon] like any app's, so it follows the user's icon style: the default is the glyph on an
 * [Ink] tile (the foreground and background layers), and the glyph, outline, tile, glass and shape settings apply to it as they do to every other icon.
 */
@Composable
fun RecentlyInstalledIcon(size: Dp, modifier: Modifier = Modifier, contentDescription: String? = null) {
    val painter = rememberVectorPainter(RecencyIcon)
    val density = LocalDensity.current
    val tile = Ink
    val glyph = InkInverted
    val layers = remember(painter, density, tile, glyph) { renderRecencyLayers(painter, density, tile, glyph) }
    AppIcon(icon = layers.tile, monochromeIcon = layers.themed, size = size, contentDescription = contentDescription, modifier = modifier)
}

/** The tile layer (glyph on a vignetted [tile]) and the themed layer (the glyph alone, in white, as an app's monochrome layer is) at [ICON_PX]. */
private fun renderRecencyLayers(painter: VectorPainter, density: Density, tile: Color, glyph: Color): RecencyLayers {
    fun layer(withTile: Boolean, glyphFraction: Float, tint: Color): ImageBitmap {
        val bitmap = ImageBitmap(ICON_PX, ICON_PX)
        CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(bitmap), Size(ICON_PX.toFloat(), ICON_PX.toFloat())) {
            if (withTile) {
                drawRect(tile)
                drawRect(brush = Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.18f)), center = center, radius = size.maxDimension * 0.75f))
            }
            val side = size.width * glyphFraction
            translate((size.width - side) / 2f, (size.height - side) / 2f) {
                with(painter) { draw(Size(side, side), colorFilter = ColorFilter.tint(tint)) }
            }
        }
        return bitmap
    }
    return RecencyLayers(tile = layer(true, GLYPH_ON_TILE, glyph), themed = layer(false, GLYPH_THEMED, Color.White))
}
