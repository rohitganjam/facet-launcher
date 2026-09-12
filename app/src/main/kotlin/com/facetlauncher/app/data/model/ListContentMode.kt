package com.facetlauncher.app.data.model

/** F2 — which source populates a facet's home app list. Favorites is manually curated; Recents/Most Used are `UsageStatsManager`-backed. */
enum class ListContentMode {
    FAVORITES,
    RECENTS,
    MOST_USED,
}
