package com.facetlauncher.app.data.model

/** The most items a facet's clock stack can hold: the native clock plus hosted widgets. */
const val MAX_CLOCK_STACK_ITEMS = 5

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
