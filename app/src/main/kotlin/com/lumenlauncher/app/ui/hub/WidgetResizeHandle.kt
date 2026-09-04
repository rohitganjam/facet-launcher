package com.lumenlauncher.app.ui.hub

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
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme

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
 * [com.lumenlauncher.app.domain.ResizeWidgetUseCase]'s own contract.
 */
@Composable
fun WidgetResizeHandle(onDrag: (Offset) -> Unit, onDragEnd: () -> Unit, testTag: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(WIDGET_RESIZE_HANDLE_TOUCH_TARGET_SIZE)
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
    LumenLauncherTheme {
        WidgetResizeHandle(onDrag = {}, onDragEnd = {}, testTag = "hub_resize_handle_preview")
    }
}
