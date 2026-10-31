package com.facetlauncher.app.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.ui.theme.FACET_TRANSITION_DURATION_MS
import com.facetlauncher.app.ui.theme.FacetTransitionEasing
import com.facetlauncher.app.ui.theme.Ink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** A drag must move the page this fraction of the zone's width, or fling faster than [PAGE_FLING_VELOCITY_PX], to turn it. */
internal const val PAGE_COMMIT_FRACTION = 0.2f
internal const val PAGE_FLING_VELOCITY_PX = 1500f

/** How long the page dots stay after a swipe ends. */
internal const val PAGE_DOTS_LINGER_MS = 2_500L

private val PAGE_SETTLE_SPEC = tween<Float>(durationMillis = FACET_TRANSITION_DURATION_MS, easing = FacetTransitionEasing)

/**
 * The swipe between the pages of the clock stack. Only one page is drawn at a time: a drag displaces it by
 * [offset] (a fraction of the zone's width, negative towards the next page) and fades it, and a committed swipe slides
 * it out, changes the page, and slides the new one in from the other side. [canMove] tells Home whether a drag
 * belongs to the pager or, at either end of the stack, to Hub and the facet carousel.
 */
internal class ClockPagerState(
    private val pageCount: () -> Int,
    private val pageIndex: () -> Int,
    private val onPageChange: (Int) -> Unit,
    private val widthPx: () -> Float,
    private val coroutineScope: CoroutineScope,
) {
    val offset = Animatable(0f)

    /** True from the first pager drag until the settle starts, so the dots can show for the whole swipe. */
    var dragActive by mutableStateOf(false)
        private set
    private var dragTarget = 0f

    /** Whether a finger moving by [dx] (positive is right) has a page to move to: left needs a next page, right a previous one. */
    fun canMove(dx: Float): Boolean = when {
        dx < 0f -> pageIndex() < pageCount() - 1
        dx > 0f -> pageIndex() > 0
        else -> false
    }

    fun dragBy(dx: Float) {
        val width = widthPx()
        if (width <= 0f) return
        if (!dragActive) {
            dragActive = true
            dragTarget = offset.value
        }
        val towardsNext = if (pageIndex() < pageCount() - 1) -1f else 0f
        val towardsPrevious = if (pageIndex() > 0) 1f else 0f
        dragTarget = (dragTarget + dx / width).coerceIn(towardsNext, towardsPrevious)
        coroutineScope.launch { offset.snapTo(dragTarget) }
    }

    /** [velocityPx] is the finger's horizontal velocity in px/s (negative is left). */
    fun settle(velocityPx: Float) {
        val moved = if (dragActive) dragTarget else offset.value
        dragActive = false
        val direction = when {
            velocityPx < -PAGE_FLING_VELOCITY_PX -> -1
            velocityPx > PAGE_FLING_VELOCITY_PX -> 1
            moved <= -PAGE_COMMIT_FRACTION -> -1
            moved >= PAGE_COMMIT_FRACTION -> 1
            else -> 0
        }
        // Moving left (direction -1) goes to the next page, so the index moves by the opposite sign.
        val target = pageIndex() - direction
        coroutineScope.launch {
            if (direction == 0 || target !in 0 until pageCount()) {
                offset.animateTo(0f, PAGE_SETTLE_SPEC)
                return@launch
            }
            offset.animateTo(direction.toFloat(), PAGE_SETTLE_SPEC)
            onPageChange(target)
            offset.snapTo(-direction.toFloat())
            offset.animateTo(0f, PAGE_SETTLE_SPEC)
        }
    }
}

/**
 * One dot per page, the visible page filled. Shown while a swipe is in progress and for [PAGE_DOTS_LINGER_MS]
 * after any page change or swipe, then faded out. Never shown on first composition, unless [forceShown] (clock adjust mode).
 */
@Composable
internal fun ClockPageDots(pageCount: Int, pageIndex: Int, dragging: Boolean, modifier: Modifier = Modifier, forceShown: Boolean = false) {
    var shown by remember { mutableStateOf(false) }
    var seenIndex by remember { mutableIntStateOf(pageIndex) }
    LaunchedEffect(pageIndex, dragging) {
        if (!(dragging || pageIndex != seenIndex || shown)) return@LaunchedEffect
        shown = true
        seenIndex = pageIndex
        if (!dragging) {
            delay(PAGE_DOTS_LINGER_MS)
            shown = false
        }
    }
    val fade = remember { Animatable(0f) }
    LaunchedEffect(shown, forceShown) { fade.animateTo(if (shown || forceShown) 1f else 0f, tween(durationMillis = 200)) }
    Row(
        modifier = modifier.alpha(fade.value).testTag("clock_page_dots"),
        horizontalArrangement = Arrangement.spacedBy(CLOCK_PAGE_DOT_GAP),
    ) {
        repeat(pageCount) { index ->
            Row(
                modifier = Modifier
                    .size(CLOCK_PAGE_DOT_SIZE)
                    .alpha(if (index == pageIndex) 1f else 0.3f)
                    .background(Ink, CircleShape)
                    .testTag("clock_page_dot_$index"),
            ) {}
        }
    }
}

private val CLOCK_PAGE_DOT_SIZE = 6.dp

/** The height of the dot row itself, used to centre it in [CLOCK_PAGE_DOTS_ROW_HEIGHT]. */
internal val CLOCK_PAGE_DOT_ROW_CONTENT = CLOCK_PAGE_DOT_SIZE
private val CLOCK_PAGE_DOT_GAP = 6.dp

/** The row the dots occupy below the divider, reserved in the list's position whenever there is more than one page. */
internal val CLOCK_PAGE_DOTS_ROW_HEIGHT = 20.dp
