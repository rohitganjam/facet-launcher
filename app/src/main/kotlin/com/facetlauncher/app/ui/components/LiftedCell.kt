package com.facetlauncher.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt

/**
 * A cell that can be picked up. While it is held and dragged, the cell itself is hidden and [copy], its look without any
 * gestures, is drawn in a window of its own at the finger, so it stays visible wherever it is dragged, past the edge of a
 * scrolling or clipping list. [content] is the cell as usual, with its gestures, and keeps receiving the touch.
 */
@Composable
internal fun LiftableCell(
    key: Any,
    copy: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val host: PickUpDragHost? = LocalPickUpReorder.current ?: LocalGridDrag.current
    val held = host != null && host.liftedKey == key && host.dragging
    var size by remember { mutableStateOf(IntSize.Zero) }
    Box(modifier = modifier.onSizeChanged { size = it }) {
        Box(modifier = Modifier.reorderItemLayer(key).gridItemLayer(key).alpha(if (held) 0f else 1f)) { content() }
        if (held && host != null && size != IntSize.Zero) LiftedPopup(host, size, copy)
    }
}

@Composable
private fun LiftedPopup(host: PickUpDragHost, size: IntSize, copy: @Composable () -> Unit) {
    val density = LocalDensity.current
    // Read here, in composition, so the popup window is moved on every step of the drag.
    val offset = host.dragOffset
    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(offset.x.roundToInt(), offset.y.roundToInt()),
        properties = PopupProperties(focusable = false, dismissOnBackPress = false, dismissOnClickOutside = false, clippingEnabled = false),
    ) {
        Box(
            modifier = Modifier
                .requiredSize(with(density) { size.width.toDp() }, with(density) { size.height.toDp() })
                .graphicsLayer { scaleX = PICK_UP_SCALE; scaleY = PICK_UP_SCALE },
        ) { copy() }
    }
}
