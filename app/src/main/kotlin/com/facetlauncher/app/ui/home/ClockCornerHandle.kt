package com.facetlauncher.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.Accent

/**
 * Corner-resize handle for the Home clock. Reports absolute touch positions in root coordinates
 * to ensure perfect tracking regardless of the handle's own movement.
 */
@Composable
fun ClockCornerHandle(
    onDrag: (touchInRoot: Offset) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
    isRightSide: Boolean = true,
    onDragStart: () -> Unit = {},
) {
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    Box(
        modifier = modifier
            .size(40.dp) // Hit target
            .onGloballyPositioned { layoutCoordinates = it }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { currentOnDragStart() },
                    onDragEnd = { currentOnDragEnd() },
                    onDragCancel = { currentOnDragEnd() }
                ) { change, _ ->
                    change.consume()
                    val coords = layoutCoordinates ?: return@detectDragGestures
                    val touchInRoot = coords.positionInRoot() + change.position
                    currentOnDrag(touchInRoot)
                }
            }
            .testTag("clock_resize_handle_${if (isRightSide) "right" else "left"}"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Accent)
        )
    }
}
