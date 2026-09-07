package com.lumenlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.R
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.Surface

/**
 * Home's clock-zone grab handle — appears (see [HomeScreen]'s long-press-on-the-clock gesture)
 * sitting below the clock+calendar block; dragging it up/down moves that whole block together with
 * it (see [HomeScreen]'s own doc on `clockZoneHeightDp`). This handle is its own dedicated hit
 * target with no other function sharing the same touch (no calendar-event tap targets sit on top
 * of it), so a plain [detectVerticalDragGestures] is enough here — no long-press gate needed on the
 * handle itself, only on the clock that reveals it. The visible affordance is a short horizontal
 * rail (an 8dp-tall line, signaling "drag line") with the Material Symbols "expand content" glyph
 * (`res/drawable/expand_content_24.xml`, rotated so its corner brackets read as an up/down resize
 * cue) sitting on top of it in its own circular badge, like a slider thumb on a track. The real hit
 * area is a full-width 48dp-tall strip — a real touch target (see CLAUDE.md's touch-target
 * conventions) even though the visible rail/icon render smaller.
 */
@Composable
fun ClockZoneHandle(
    onDrag: (deltaDp: Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    // pointerInput(Unit) launches its gesture-detection coroutine once and never restarts it, so
    // the callbacks it closes over must go through rememberUpdatedState — otherwise it would keep
    // calling whichever onDrag/onDragEnd instance was passed in on the very first composition,
    // silently going stale the moment HomeScreen recomposes with fresh closures (same reasoning
    // com.lumenlauncher.app.ui.components.DragReorderState already applies to its own callbacks).
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                // A fixed corner radius (half the bar's own height), not CircleShape — CircleShape
                // clips to an ellipse inscribed in the full bounds, which on a wide-short box would
                // squash into a lens shape instead of a pill with flat long edges.
                .clip(RoundedCornerShape(4.dp))
                .background(Muted),
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.expand_content_24),
                contentDescription = "Drag to resize",
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
    LumenLauncherTheme {
        ClockZoneHandle(onDrag = {}, onDragEnd = {})
    }
}
