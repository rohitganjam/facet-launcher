package com.facetlauncher.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.GlassStyle
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Surface
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls

private fun fill(top: Float, bottom: Float, color: Color = Color.White) =
    Brush.verticalGradient(listOf(color.copy(alpha = top), color.copy(alpha = bottom)))

private fun edge(top: Float, bottom: Float, color: Color = Color.White) = fill(top, bottom, color)

/** The diagonal edge of [GlassStyle.CLEAR_GLASS]: bright at the top left and bottom right, fading between. */
private val ClearGlassEdge = Brush.linearGradient(
    listOf(Color.White.copy(alpha = 0.75f), Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.35f)),
)

/**
 * A frosted-glass panel behind the content, drawn per [style]. It tints rather than blurs, since Compose can't blur the
 * wallpaper behind the window. No-op for [GlassStyle.OFF].
 */
@Composable
fun Modifier.glassBackground(style: GlassStyle, shape: Shape = MaterialTheme.shapes.medium, sampleWallpaper: Boolean = false): Modifier {
    val accent = MaterialTheme.colorScheme.primary
    // Only a caller that opts in samples the wallpaper copy; the rest keep the tint.
    val backdrop = LocalWallpaperBackdrop.current.takeIf { sampleWallpaper }
    val backdropTint = LocalBackdropTint.current.takeIf { sampleWallpaper }
    return when (style) {
        GlassStyle.OFF -> this
        GlassStyle.FROST -> glassPanel(shape, fill(0.22f, 0.10f), edge(0.35f, 0.08f), backdrop, FrostLook, backdropTint)
        GlassStyle.DARK_FROST -> glassPanel(shape, fill(0.42f, 0.42f, Smoke), edge(0.18f, 0.08f), backdrop, DarkFrostLook, backdropTint)
        GlassStyle.BRIGHT -> glassPanel(shape, fill(0.38f, 0.24f), edge(0.6f, 0.25f), backdrop, BrightLook, backdropTint)
        GlassStyle.CLEAR_GLASS -> glassPanel(shape, fill(0.07f, 0.05f), ClearGlassEdge, backdrop, ClearLook, backdropTint)
        GlassStyle.DARK_GLASS -> glassPanel(shape, fill(0.42f, 0.42f, Smoke), ClearGlassEdge, backdrop, DarkGlassLook, backdropTint)
        GlassStyle.ACCENT -> glassPanel(shape, fill(0.38f, 0.18f, accent), edge(0.45f, 0.15f, accent), backdrop, AccentLook, backdropTint)
        GlassStyle.THEME -> solidPanel(shape, Surface.copy(alpha = THEME_FILL_ALPHA), Hairline)
        GlassStyle.THEME_INVERTED -> solidPanel(shape, Ink.copy(alpha = THEME_FILL_ALPHA), Ink.copy(alpha = THEME_FILL_ALPHA))
    }
}

/** How opaque the Theme and Theme inverted fills are: the widget backing before glass existed, so the wallpaper still shows through. */
private const val THEME_FILL_ALPHA = 0.6f

private fun Modifier.solidPanel(shape: Shape, color: Color, edge: Color): Modifier = clip(shape).background(color).border(1.dp, edge, shape)

private fun Modifier.glassPanel(shape: Shape, fill: Brush, edge: Brush, backdrop: LayerBackdrop?, look: BackdropLook, backdropTint: Color? = null): Modifier =
    if (backdrop == null) {
        clip(shape).background(fill).border(1.dp, edge, shape)
    } else {
        drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                colorControls(look.brightness, 1f, look.saturation)
                blur(look.blurDp.dp.toPx())
                // Clear glass bends the wallpaper at the rim; the lens needs API 33, below that it does nothing.
                if (look.lens && shape is CornerBasedShape) clearGlassLens(shape, LENS_HEIGHT_DP.dp.toPx(), LENS_AMOUNT_DP.dp.toPx(), LENS_DEPTH_WEIGHT)
            },
            highlight = null,
            shadow = null,
            onDrawSurface = {
                backdropTint?.let { drawRect(it) }
                drawRect(look.glassFill ?: fill)
            },
        ).border(1.dp, edge, shape)
    }

/** What a style does to the wallpaper copy behind it: how much it blurs, how it brightens and saturates it, and (for a lighter wash than the tint alone) its own fill. */
private class BackdropLook(val blurDp: Float, val brightness: Float = 0f, val saturation: Float = 1f, val glassFill: Brush? = null, val lens: Boolean = false)

/** Milky and diffuse: the heaviest blur under a soft white wash. */
private val FrostLook = BackdropLook(blurDp = 16f, saturation = 1.5f, lens = true)

private val Smoke = Color(0xFF0A0C18)

/** Dark and calm, with the colour kept close to natural. */
private val DarkFrostLook = BackdropLook(blurDp = 12f, brightness = -0.04f, saturation = 1.15f, lens = true)

/** Clear glass, darker: the wallpaper stays sharp and bends at the rim, under a dark wash. */
private val DarkGlassLook = BackdropLook(blurDp = 1.5f, brightness = -0.05f, glassFill = fill(0.30f, 0.30f, Smoke), lens = true)

/** Crisp and luminous: a light blur, a brightened and more vivid backdrop, and only a thin wash. */
private val BrightLook = BackdropLook(blurDp = 7f, brightness = 0.10f, saturation = 1.4f, glassFill = fill(0.12f, 0.04f), lens = true)

private val AccentLook = BackdropLook(blurDp = 12f, saturation = 1.5f, lens = true)

/** The wallpaper stays sharp and bends at the rim. */
private val ClearLook = BackdropLook(blurDp = 1.5f, lens = true)

private const val LENS_HEIGHT_DP = 14f
private const val LENS_AMOUNT_DP = 24f

/** How much of a pull away from the panel's centre is added to the edge-normal bend; 0 is none and 1 is the library's depth effect. */
private const val LENS_DEPTH_WEIGHT = 0.8f
