package com.facetlauncher.app.ui.drawer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.DrawerRailTextColor
import kotlin.math.abs
import kotlin.math.cos

/** Vertical gap between rail letters when there's room for it — see [AlphabetRail]'s own doc. */
private val RAIL_LETTER_SPACING = 6.dp

/** Peak scale for the active letter itself (index-distance 0). */
private const val MAGNIFY_MAX_SCALE = 1.7f

/** How many letters out the magnify effect tapers over — a gradual cosine falloff, not discrete steps, so neighbors ease smoothly back to rest scale rather than stepping down abruptly. */
private const val MAGNIFY_FALLOFF_RADIUS = 4

/** How far (px) an enlarged letter shifts left, toward the app list, per unit of scale above 1 — a subtle "pop toward the finger" cue, not just growth in place. */
private const val MAGNIFY_SHIFT_PX_PER_SCALE_UNIT = 200f

/** A smooth (cosine half-bell) falloff from [MAGNIFY_MAX_SCALE] at `distance = 0` down to `1f` by [MAGNIFY_FALLOFF_RADIUS] letters out. */
private fun magnifyScale(distance: Int): Float {
    if (distance >= MAGNIFY_FALLOFF_RADIUS) return 1f
    val falloff = 0.5f * (1f + cos((Math.PI * distance / MAGNIFY_FALLOFF_RADIUS)).toFloat())
    return 1f + (MAGNIFY_MAX_SCALE - 1f) * falloff
}

/**
 * Right-edge alphabet rail (`1h`/`1i`) — a purely visual list of only the letters currently
 * present in the drawer's app list (not the full alphabet). Touch handling lives one level up,
 * in [AppDrawerScreen]'s activation zone, which spans 90% of the drawer height on both edges —
 * see [letterAt].
 *
 * A custom [Layout] instead of a plain `Column`, because a `Column` has no way to shrink its own
 * spacing when its content doesn't fit — with enough letters (a long app list touches every
 * letter) or a large system font-scale setting (each `Text` grows in `sp`, same as any other
 * text in the app), a fixed [RAIL_LETTER_SPACING] can overflow past the caller's height
 * constraint and clip letters off-screen, even though [letterAt]'s own mapping still covers the
 * full list correctly (confirmed on-device — scrolling through the clipped letters still works,
 * only the visual rendering was broken; see chat history). This measures every letter first,
 * then picks the largest spacing that still fits the incoming height constraint (down to `0`),
 * so the rail always renders every letter regardless of how many there are or how large the
 * system font scale is set.
 *
 * [activeLetter] also drives a magnify-on-touch effect (per direct reference to another
 * launcher's rail, see chat history): the active letter and its nearby neighbors scale up and
 * shift slightly toward the app list, easing smoothly back to rest (scale `1`) over
 * [MAGNIFY_FALLOFF_RADIUS] letters — see [magnifyScale]. Scaling is a [graphicsLayer] transform
 * (draw-time only), so it doesn't feed back into the spacing math above — enlarged letters can
 * overlap their neighbors slightly, matching the reference's look, rather than pushing the
 * whole rail wider.
 */
@Composable
fun AlphabetRail(
    letters: List<String>,
    modifier: Modifier = Modifier,
    activeLetter: String? = null,
) {
    val activeIndex = activeLetter?.let { letters.indexOf(it) }?.takeIf { it >= 0 }
    Layout(
        content = {
            letters.forEachIndexed { index, letter ->
                val targetScale = activeIndex?.let { active -> magnifyScale(abs(index - active)) } ?: 1f
                val scale by animateFloatAsState(
                    targetValue = targetScale,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 700f),
                    label = "railLetterScale",
                )
                Text(
                    text = letter,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (letter == activeLetter) Accent else DrawerRailTextColor,
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = -(scale - 1f) * MAGNIFY_SHIFT_PX_PER_SCALE_UNIT
                    },
                )
            }
        },
        modifier = modifier.width(34.dp),
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(Constraints()) }
        val totalTextHeight = placeables.sumOf { it.height }
        val gapCount = (placeables.size - 1).coerceAtLeast(0)
        val naturalSpacingPx = RAIL_LETTER_SPACING.roundToPx()
        val availableHeight = constraints.maxHeight.takeIf { it != Constraints.Infinity }
        val spacingPx = when {
            gapCount == 0 -> 0
            availableHeight == null -> naturalSpacingPx
            else -> minOf(naturalSpacingPx, ((availableHeight - totalTextHeight) / gapCount).coerceAtLeast(0))
        }
        val totalHeight = (totalTextHeight + spacingPx * gapCount).coerceAtMost(availableHeight ?: Int.MAX_VALUE)
        val width = placeables.maxOfOrNull { it.width } ?: 0

        layout(width, totalHeight) {
            var y = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(x = (width - placeable.width) / 2, y = y)
                y += placeable.height + spacingPx
            }
        }
    }
}

/**
 * Maps a y offset (in the activation zone's coordinate space, which spans the full drawer
 * height) onto one of [letters]. A touch above the visible rail band ([bandTopPx]..[bandBottomPx])
 * clamps to the first letter; below it clamps to the last; inside it maps proportionally —
 * so the whole edge is a forgiving activation target, not just the narrow rail itself.
 */
internal fun letterAt(y: Float, bandTopPx: Float, bandBottomPx: Float, letters: List<String>): String? {
    if (letters.isEmpty()) return null
    val bandHeight = bandBottomPx - bandTopPx
    if (bandHeight <= 0f) return null
    val relativeY = (y - bandTopPx).coerceIn(0f, bandHeight)
    val index = ((relativeY / bandHeight) * letters.size).toInt().coerceIn(0, letters.size - 1)
    return letters[index]
}
