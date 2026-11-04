package com.facetlauncher.app.data.model

/** Which placed widget Facet draws itself: one on the Hub grid, or one page of a facet's Home clock stack. Hub and Home share every code path through this. */
sealed interface NativeWidgetRef {
    data class Hub(val appWidgetId: Int) : NativeWidgetRef
    data class Home(val itemId: Long) : NativeWidgetRef
}
