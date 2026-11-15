package com.facetlauncher.app.data.model

/**
 * What drawer search learns from use, keyed by [AppInfo.listKey]: [picks] is how often each app was opened from search, per normalized
 * query, and [launchCounts] how often each app was opened from Facet in the Most used window.
 */
data class SearchSignals(
    val picks: Map<String, Map<String, Int>> = emptyMap(),
    val launchCounts: Map<String, Int> = emptyMap(),
) {
    companion object {
        val EMPTY = SearchSignals()
    }
}
