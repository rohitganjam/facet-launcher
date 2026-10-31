package com.facetlauncher.app.data.model

/** The most items a facet's clock stack can hold with Pro: the native clock plus hosted widgets. */
const val MAX_CLOCK_STACK_ITEMS = 5

/** Free keeps the one page every facet starts with; more than one page is Pro. */
const val FREE_CLOCK_STACK_ITEMS = 1

fun clockStackLimit(isPro: Boolean): Int = if (isPro) MAX_CLOCK_STACK_ITEMS else FREE_CLOCK_STACK_ITEMS

enum class ClockStackItemKind { CLOCK, WIDGET }

/**
 * One page of a facet's clock stack. A [ClockStackItemKind.CLOCK] item draws the facet's own native
 * clock and uses the facet's clock scale and alignment, so its size and alignment fields stay null.
 * A [ClockStackItemKind.WIDGET] item is a hosted widget with its own size and alignment; `null`
 * sizes mean "not resized yet" and landscape's `null`s fall back to portrait.
 */
data class ClockStackItem(
    val id: Long,
    val facetId: Long,
    val position: Int,
    val kind: ClockStackItemKind,
    val appWidgetId: Int? = null,
    val widthDp: Int? = null,
    val heightDp: Int? = null,
    val alignment: ClockAlignment? = null,
    val landscapeWidthDp: Int? = null,
    val landscapeHeightDp: Int? = null,
    val landscapeAlignment: ClockAlignment? = null,
)

/** The page every facet starts with: its native clock. A real row gets its ids when stored. */
fun nativeClockPage(facetId: Long = 0L, id: Long = 0L) = ClockStackItem(id = id, facetId = facetId, position = 0, kind = ClockStackItemKind.CLOCK)
