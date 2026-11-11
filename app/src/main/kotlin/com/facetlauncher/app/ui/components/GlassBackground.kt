package com.facetlauncher.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.GlassStyle

private fun fill(top: Float, bottom: Float, color: Color = Color.White) =
    Brush.verticalGradient(listOf(color.copy(alpha = top), color.copy(alpha = bottom)))

private fun edge(top: Float, bottom: Float, color: Color = Color.White) = fill(top, bottom, color)

/** The diagonal edge of [GlassStyle.RIM_LIGHT]: bright at the top left and bottom right, fading between. */
private val RimEdge = Brush.linearGradient(
    listOf(Color.White.copy(alpha = 0.75f), Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.35f)),
)

/**
 * A frosted-glass panel behind the content, drawn per [style]. It tints rather than blurs, since Compose can't blur the
 * wallpaper behind the window. No-op for [GlassStyle.OFF].
 */
@Composable
fun Modifier.glassBackground(style: GlassStyle, shape: Shape = MaterialTheme.shapes.medium): Modifier {
    val accent = MaterialTheme.colorScheme.primary
    return when (style) {
        GlassStyle.OFF -> this
        GlassStyle.FROST -> glassPanel(shape, fill(0.22f, 0.10f), edge(0.35f, 0.08f))
        GlassStyle.SMOKED -> glassPanel(shape, fill(0.42f, 0.42f, Color(0xFF0A0C18)), edge(0.18f, 0.08f))
        GlassStyle.BRIGHT -> glassPanel(shape, fill(0.38f, 0.24f), edge(0.6f, 0.25f))
        GlassStyle.RIM_LIGHT -> glassPanel(shape, fill(0.07f, 0.05f), RimEdge)
        GlassStyle.ACCENT -> glassPanel(shape, fill(0.38f, 0.18f, accent), edge(0.45f, 0.15f, accent))
    }
}

private fun Modifier.glassPanel(shape: Shape, fill: Brush, edge: Brush): Modifier =
    clip(shape).background(fill).border(1.dp, edge, shape)
