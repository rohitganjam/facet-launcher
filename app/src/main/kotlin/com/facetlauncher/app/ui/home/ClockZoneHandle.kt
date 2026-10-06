package com.facetlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Faint
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.Surface

/** The rail halo's opacity — enough edge to separate it from a similar wallpaper, soft enough not to read as an outline. */
private const val RAIL_HALO_ALPHA = 0.5f

/** Height of the handle's full-width touch strip (the 32dp knob sits centered in it) — also what the alignment toolbar clears. */
internal val CLOCK_ZONE_HANDLE_TOUCH_HEIGHT = 48.dp

/**
 * Home's clock-zone grab handle — appears (see [HomeScreen]'s long-press-on-the-clock gesture)
 * sitting below the clock+calendar block; dragging it up/down moves that whole block together with
 * it (see [HomeScreen]'s own doc on `clockZoneHeightDp`). This handle is its own dedicated hit
 * target with no other function sharing the same touch (no calendar-event tap targets sit on top
 * of it), so a plain [detectVerticalDragGestures] is enough here — no long-press gate needed on the
 * handle itself, only on the clock that reveals it. The visible affordance is a short horizontal
 * rail (a 10dp-tall line, signaling "drag line") with the Material Symbols "expand content" glyph
 * (`res/drawable/expand_content_24.xml`, rotated so its corner brackets read as an up/down resize
 * cue) sitting on top of it in its own circular badge, like a slider thumb on a track. Both carry
 * an outline and a soft shadow, because the handle sits straight on the wallpaper and a flat fill
 * disappears into a similar one: the rail is a [Muted] core with a translucent [Surface] halo (a light edge on a dark
 * wallpaper, a dark edge on a light one — translucent because a solid one reads as a hard outline), the knob a [Surface] disc with a faint [Faint] ring. The real hit
 * area is a full-width 48dp-tall strip — a real touch target (see CLAUDE.md's touch-target
 * conventions) even though the visible rail/icon render smaller.
 */
@Composable
fun ClockZoneHandle(
    onDrag: (deltaDp: Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
    onDragStart: () -> Unit = {},
) {
    val density = LocalDensity.current
    // pointerInput(Unit) launches its gesture-detection coroutine once and never restarts it, so
    // the callbacks it closes over must go through rememberUpdatedState — otherwise it would keep
    // calling whichever onDrag/onDragEnd instance was passed in on the very first composition,
    // silently going stale the moment HomeScreen recomposes with fresh closures (same reasoning
    // com.facetlauncher.app.ui.components.DragReorderState already applies to its own callbacks).
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(CLOCK_ZONE_HANDLE_TOUCH_HEIGHT)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { currentOnDragStart() },
                    onDragEnd = { currentOnDragEnd() },
                    onDragCancel = { currentOnDragEnd() },
                ) { change, dragAmountPx ->
                    change.consume()
                    currentOnDrag(with(density) { dragAmountPx.toDp().value })
                }
            }
            .testTag("home_clock_zone_handle"),
        contentAlignment = Alignment.Center,
    ) {
        // The rail — a full-width horizontal line signaling "this is a drag line" — sits behind
        // the icon, which reads as the thumb/grip on top of it.
        // A fixed corner radius (half the bar's own height), not CircleShape — CircleShape clips to an
        // ellipse inscribed in the full bounds, which on a wide-short box would squash into a lens
        // shape instead of a pill with flat long edges. 10dp tall with a 1dp halo leaves an 8dp core.
        val railShape = RoundedCornerShape(5.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .shadow(elevation = 2.dp, shape = railShape)
                .background(Muted, railShape)
                .border(1.dp, Surface.copy(alpha = RAIL_HALO_ALPHA), railShape),
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .shadow(elevation = 4.dp, shape = CircleShape)
                .background(Surface, CircleShape)
                .border(1.dp, Faint, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.expand_content_24),
                contentDescription = stringResource(R.string.clock_zone_handle_drag_to_resize),
                tint = Muted,
                modifier = Modifier.padding(4.dp).rotate(315f),
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ClockZoneHandlePreview() {
    FacetLauncherTheme {
        ClockZoneHandle(onDrag = {}, onDragEnd = {})
    }
}
