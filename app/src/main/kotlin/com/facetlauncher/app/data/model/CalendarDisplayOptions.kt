package com.facetlauncher.app.data.model

/** How Home's calendar events render — the effective (facet-or-global) values of the two display settings. */
data class CalendarDisplayOptions(
    val showIndicator: Boolean = true,
    val itemsToShow: Int = MAX_ITEMS,
) {
    companion object {
        const val MIN_ITEMS = 1
        const val MAX_ITEMS = 3
    }
}
