package com.facetlauncher.app.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.facetlauncher.app.data.model.MediaPlayerState

/**
 * What `HomeScreen` measures and what its adjust-mode drags hold in flight, in one place so each part of the
 * screen takes this instead of two dozen separate variables.
 *
 * Measured sizes and positions are stored as primitives (`IntSize`, `Offset`), not as `LayoutCoordinates`:
 * `onGloballyPositioned` reuses one coordinates instance and only mutates it, so storing it would dedupe to a
 * no-op and never recompose when, say, the clock box changes size on a template switch.
 */
@Stable
internal class HomeScreenLayout {
    /** The content area's height, and the clock block's intrinsic height (offset-independent, so a stable one-frame-lag measurement). */
    var contentHeightPx by mutableFloatStateOf(0f)
    var clockNaturalHeightPx by mutableFloatStateOf(0f)

    /** Landscape: the apps pane's width (to narrow the grid to what fits), the content's width, and the bottom dock row's height. */
    var appsPaneWidthPx by mutableFloatStateOf(0f)
    var landscapeContentWidthPx by mutableFloatStateOf(0f)
    var landscapeDockRowHeightPx by mutableFloatStateOf(0f)

    /** Landscape: the clock pane's height, which a page is aligned within (top, middle or bottom). */
    var landscapeClockPaneHeightPx by mutableFloatStateOf(0f)

    /**
     * The app list's natural (unconstrained) content height, measured inside its scroll region, and whether its
     * rows therefore drop to the drawer's compact density (8dp padding instead of 16dp).
     */
    var appListNaturalHeightPx by mutableFloatStateOf(0f)
    var useCompactAppSpacing by mutableStateOf(false)

    /**
     * Live values while dragging, held past the drag's end until the committed value round-trips through the
     * repository, so there is no flicker back to the old value for the frames in between. They are absolute
     * values, not deltas (a delta double-counts against the committed value in the frame the commit lands).
     * [liveDividerWidthPx] is landscape's divider, [liveHandlePx] portrait's move handle, [liveScale] the native
     * clock's scale, and the widget pair a hosted widget's real size.
     */
    var liveDividerWidthPx by mutableStateOf<Float?>(null)
    var liveHandlePx by mutableStateOf<Float?>(null)
    var liveScale by mutableStateOf<Float?>(null)
    var liveWidgetWidthPx by mutableStateOf<Float?>(null)
    var liveWidgetHeightPx by mutableStateOf<Float?>(null)

    /** The root, the clock box, the whole portrait clock block's bottom edge (measured in the same pass as the box), and the app list. */
    var rootSize by mutableStateOf<IntSize?>(null)
    var rootOriginInRoot by mutableStateOf<Offset?>(null)
    var clockBoxSize by mutableStateOf<IntSize?>(null)
    var clockBoxOriginInRoot by mutableStateOf<Offset?>(null)
    var clockBlockBottomInRoot by mutableStateOf<Float?>(null)
    var appListSize by mutableStateOf<IntSize?>(null)
    var appListOriginInRoot by mutableStateOf<Offset?>(null)

    /**
     * Where a drag grabbed relative to the value it implies, captured on the first move so nothing pops: NaN
     * until then for the native clock, `null` for a hosted widget.
     */
    var dragGrabOffset by mutableFloatStateOf(Float.NaN)
    var widgetDragGrabOffset by mutableStateOf<Offset?>(null)

    /** The media row sits under the clock; its height and the last player are kept across hides so adjust mode (which hides it) doesn't shift the list. */
    var lastMediaPlayer by mutableStateOf<MediaPlayerState?>(null)
    var mediaRowHeightPx by mutableFloatStateOf(0f)
}

@Composable
internal fun rememberHomeScreenLayout(): HomeScreenLayout = remember { HomeScreenLayout() }
