package com.lumenlauncher.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * Pins [header] above [content], which keeps scrolling beneath it rather than under it — every
 * screen with a title/back-button row used to put that row as the first item in its own scrolling
 * list, so it scrolled away with everything else (see chat history: caught while scoping the
 * Widget Hub's own header, then generalized here since it was really an app-wide gap, not a
 * Hub-only one). [content] receives the header's own measured height in [Dp] so it can pad its
 * scrollable area by exactly that much — not a guessed constant, so a header whose content varies
 * (e.g. a subtitle that wraps to two lines) still lines up correctly. [header] must paint its own
 * opaque background (matching the screen's own background token): it's drawn on top of [content]
 * in the same [Box], so without one, content scrolled underneath it would show through.
 */
@Composable
fun StickyHeaderLayout(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    content: @Composable (headerHeight: Dp) -> Unit,
) {
    val density = LocalDensity.current
    var headerHeightPx by remember { mutableIntStateOf(0) }

    Box(modifier = modifier.fillMaxSize()) {
        content(with(density) { headerHeightPx.toDp() })
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onGloballyPositioned { headerHeightPx = it.size.height },
        ) {
            header()
        }
    }
}
