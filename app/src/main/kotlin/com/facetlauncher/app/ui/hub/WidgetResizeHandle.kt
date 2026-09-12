package com.facetlauncher.app.ui.hub

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme

/** [HubGrid] straddles this half-on-half-off the widget's own edge, matching a standard resize-handle convention. */
val WIDGET_RESIZE_HANDLE_SIZE = 24.dp

/**
 * The handle's actual touch target, bigger than the visible dot above. A touch that misses the
 * 24dp dot by even a couple dp used to fall through to the widget tile's own long-press gesture
 * underneath, which treats any touch that starts moving before it becomes a long-press as "a tap"
 * and immediately dismisses resize mode (see chat history) — so near-misses on the handle would
 * silently cancel the resize the user was trying to perform. [HubGrid] computes the half-on-half-
 * off straddle offset off this touch target size, not the visible dot's, so the dot itself stays
 * pinned exactly on the widget's edge.
 */
val WIDGET_RESIZE_HANDLE_TOUCH_TARGET_SIZE = 44.dp

/**
 * One edge-adjustment badge shown while a widget is in resize mode — [HubGrid] places one on the
 * right edge (dragging it grows/shrinks width only) and one on the bottom edge (height only),
 * rather than a single corner handle, so which dimension a drag changes is obvious from which
 * edge is grabbed. Either way the top-left cell it's anchored at never moves, matching
 * [com.facetlauncher.app.domain.ResolveWidgetResizeUseCase]'s own contract.
 */
@Composable
fun WidgetResizeHandle(
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    // Only [HubGrid]'s top handle ever overrides these — its own touch target is clamped
    // narrower than the full square when there isn't enough room above the widget to straddle
    // (see the top handle's own call site for why), and this keeps the *reach into the widget's
    // own interior* fixed at half the standard target rather than growing to fill whatever's left
    // over when the exterior overhang shrinks (see chat history — an earlier version just slid the
    // full-size square down instead of shrinking it, so a fully-clamped handle ballooned inward to
    // cover nearly half the tile, stealing ordinary long-press-to-regrab touches).
    touchTargetWidth: Dp = WIDGET_RESIZE_HANDLE_TOUCH_TARGET_SIZE,
    touchTargetHeight: Dp = WIDGET_RESIZE_HANDLE_TOUCH_TARGET_SIZE,
) {
    Box(
        modifier = modifier
            .size(width = touchTargetWidth, height = touchTargetHeight)
            .testTag(testTag)
            // Excludes this handle's area from system gestures (like the back swipe)
            // so grabbing it near the screen edge doesn't trigger navigation.
            .systemGestureExclusion()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd,
                ) { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(WIDGET_RESIZE_HANDLE_SIZE)
                .background(Accent, CircleShape),
        )
    }
}

@Preview(showBackground = true, widthDp = 80, heightDp = 80)
@Preview(name = "Dark", showBackground = true, widthDp = 80, heightDp = 80, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WidgetResizeHandlePreview() {
    FacetLauncherTheme {
        WidgetResizeHandle(onDrag = {}, onDragEnd = {}, testTag = "hub_resize_handle_preview")
    }
}
