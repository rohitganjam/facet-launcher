package com.facetlauncher.app.ui.drawer

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder as FolderIcon
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.DrawerRailTextColor
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.SurfaceContainer
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

/** Where the drawer's folder-shortcut glyph sits relative to the letters — mirrors the App Drawer's "Show folders first/last" setting. [NONE] omits the glyph (folders hidden, or shown inline with the letters, where they need no separate rail entry). */
enum class RailFolderPosition { NONE, TOP, BOTTOM }

/** What a rail drag/touch resolved to — a specific [Letter], or [Folders] when [RailFolderPosition] is active and the touch landed on that end's glyph slot. */
sealed interface RailSelection {
    data class Letter(val letter: String) : RailSelection
    data object Folders : RailSelection
}

/**
 * Right-edge alphabet rail (`1h`/`1i`) — a purely visual list of only the letters currently
 * present in the drawer's app list (not the full alphabet), optionally capped at one end by a
 * folder glyph ([folderPosition]) when the App Drawer is showing a pinned Folders section rather
 * than interleaving folders alphabetically. Touch handling lives one level up, in
 * [AppDrawerScreen]'s activation zone, which spans 90% of the drawer height on both edges — see
 * [letterAt].
 *
 * A custom [Layout] instead of a plain `Column`, because a `Column` has no way to shrink its own
 * spacing when its content doesn't fit — with enough letters (a long app list touches every
 * letter) or a large system font-scale setting (each `Text` grows in `sp`, same as any other
 * text in the app), a fixed [RAIL_LETTER_SPACING] can overflow past the caller's height
 * constraint and clip letters off-screen, even though [letterAt]'s own mapping still covers the
 * full list correctly (confirmed on-device — scrolling through the clipped letters still works,
 * only the visual rendering was broken; see chat history). This measures every child — letters
 * and the optional folder glyph alike, since the [Layout] treats them uniformly — first, then
 * picks the largest spacing that still fits the incoming height constraint (down to `0`), so the
 * rail always renders every entry regardless of how many there are or how large the system font
 * scale is set. [RailFolderGlyph] sizes itself off the same [MaterialTheme.typography.labelSmall]
 * metrics as the letters, so it grows and shrinks in lockstep with them under any font scale.
 *
 * [activeLetter] also drives a magnify-on-touch effect (per direct reference to another
 * launcher's rail, see chat history): the active letter and its nearby neighbors scale up and
 * shift slightly toward the app list, easing smoothly back to rest (scale `1`) over
 * [MAGNIFY_FALLOFF_RADIUS] letters — see [magnifyScale]. Scaling is a [graphicsLayer] transform
 * (draw-time only), so it doesn't feed back into the spacing math above — enlarged letters can
 * overlap their neighbors slightly, matching the reference's look, rather than pushing the
 * whole rail wider. The folder glyph doesn't participate in this magnify effect — only
 * [folderActive] toggles its tint, matching how a letter's tint (not its scale) marks it as the
 * exact drag target.
 */
@Composable
fun AlphabetRail(
    letters: List<String>,
    modifier: Modifier = Modifier,
    activeLetter: String? = null,
    folderPosition: RailFolderPosition = RailFolderPosition.NONE,
    folderActive: Boolean = false,
) {
    val activeIndex = activeLetter?.let { letters.indexOf(it) }?.takeIf { it >= 0 }
    Layout(
        content = {
            if (folderPosition == RailFolderPosition.TOP) {
                RailFolderGlyph(active = folderActive)
            }
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
            if (folderPosition == RailFolderPosition.BOTTOM) {
                RailFolderGlyph(active = folderActive)
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
 * The rail's folder-shortcut entry — sized to [MaterialTheme.typography.labelSmall]'s own font
 * size (converted through [LocalDensity], which folds in the system font-scale setting the same
 * way `sp` does for the letters' `Text`), so the glyph tracks a letter's footprint 1:1 at any
 * accessibility text size rather than sitting at a fixed `dp` that only matches at scale `1`.
 * Tints [Accent] when [active] (this end of the rail is the current drag target), mirroring how
 * an active letter recolors rather than resizes.
 */
@Composable
private fun RailFolderGlyph(active: Boolean, modifier: Modifier = Modifier) {
    val glyphSize = with(LocalDensity.current) { MaterialTheme.typography.labelSmall.fontSize.toDp() }
    Icon(
        imageVector = Icons.Outlined.FolderIcon,
        contentDescription = "Folders",
        tint = if (active) Accent else DrawerRailTextColor,
        modifier = modifier.size(glyphSize),
    )
}

/**
 * Maps a y offset (in the activation zone's coordinate space, which spans the full drawer
 * height) onto one of [letters]. A touch above the visible rail band ([bandTopPx]..[bandBottomPx])
 * clamps to the first letter; below it clamps to the last; inside it maps proportionally —
 * so the whole edge is a forgiving activation target, not just the narrow rail itself.
 */
internal fun letterAt(y: Float, bandTopPx: Float, bandBottomPx: Float, letters: List<String>): String? =
    (railSelectionAt(y, bandTopPx, bandBottomPx, letters, RailFolderPosition.NONE) as? RailSelection.Letter)?.letter

/**
 * [letterAt]'s generalization: same proportional band-clamping, but when [folderPosition] isn't
 * [RailFolderPosition.NONE] the folder glyph occupies one more equal slot at that end (matching
 * [AlphabetRail]'s own layout, which gives the glyph the same footprint as a letter — see
 * [RailFolderGlyph]'s own doc), so a touch landing there resolves to [RailSelection.Folders]
 * instead of being folded into the nearest real letter.
 */
internal fun railSelectionAt(
    y: Float,
    bandTopPx: Float,
    bandBottomPx: Float,
    letters: List<String>,
    folderPosition: RailFolderPosition,
): RailSelection? {
    val slotCount = letters.size + if (folderPosition == RailFolderPosition.NONE) 0 else 1
    if (slotCount == 0) return null
    val bandHeight = bandBottomPx - bandTopPx
    if (bandHeight <= 0f) return null
    val relativeY = (y - bandTopPx).coerceIn(0f, bandHeight)
    val index = ((relativeY / bandHeight) * slotCount).toInt().coerceIn(0, slotCount - 1)
    return when (folderPosition) {
        RailFolderPosition.NONE -> RailSelection.Letter(letters[index])
        RailFolderPosition.TOP -> if (index == 0) RailSelection.Folders else RailSelection.Letter(letters[index - 1])
        RailFolderPosition.BOTTOM -> if (index == letters.size) RailSelection.Folders else RailSelection.Letter(letters[index])
    }
}

private val PREVIEW_LETTERS = listOf("A", "B", "C", "D", "E", "F", "G", "H")

@Composable
private fun AlphabetRailPreviewSurface(content: @Composable () -> Unit) {
    FacetLauncherTheme {
        Box(modifier = Modifier.fillMaxSize().background(SurfaceContainer)) {
            content()
        }
    }
}

@Preview(name = "Folders first", showBackground = true, widthDp = 60, heightDp = 360)
@Preview(name = "Folders first — dark", showBackground = true, widthDp = 60, heightDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AlphabetRailFoldersFirstPreview() {
    AlphabetRailPreviewSurface {
        AlphabetRail(letters = PREVIEW_LETTERS, activeLetter = "C", folderPosition = RailFolderPosition.TOP)
    }
}

@Preview(name = "Folders last", showBackground = true, widthDp = 60, heightDp = 360)
@Preview(name = "Folders last — dark", showBackground = true, widthDp = 60, heightDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AlphabetRailFoldersLastPreview() {
    AlphabetRailPreviewSurface {
        AlphabetRail(letters = PREVIEW_LETTERS, activeLetter = "F", folderPosition = RailFolderPosition.BOTTOM)
    }
}

/** Dragged past the last letter, onto the folder glyph itself — [RailFolderGlyph] tints [Accent] exactly like an active letter would. */
@Preview(name = "Folder glyph active", showBackground = true, widthDp = 60, heightDp = 360)
@Composable
private fun AlphabetRailFolderActivePreview() {
    AlphabetRailPreviewSurface {
        AlphabetRail(letters = PREVIEW_LETTERS, folderPosition = RailFolderPosition.BOTTOM, folderActive = true)
    }
}

/** Same rail content at three system font-scale settings — [RailFolderGlyph] must grow in lockstep with the letters at every scale, never sitting fixed-size while they grow around it. */
@Preview(name = "Font scale 1x", showBackground = true, widthDp = 60, heightDp = 500, fontScale = 1f)
@Preview(name = "Font scale 1.5x", showBackground = true, widthDp = 60, heightDp = 500, fontScale = 1.5f)
@Preview(name = "Font scale 2x (accessibility max)", showBackground = true, widthDp = 60, heightDp = 500, fontScale = 2f)
@Composable
private fun AlphabetRailFontScalePreview() {
    AlphabetRailPreviewSurface {
        AlphabetRail(
            letters = ('A'..'Z').map { it.toString() },
            activeLetter = "M",
            folderPosition = RailFolderPosition.TOP,
        )
    }
}
