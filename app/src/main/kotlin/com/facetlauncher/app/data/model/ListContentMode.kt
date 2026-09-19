package com.facetlauncher.app.data.model

import androidx.annotation.StringRes
import com.facetlauncher.app.R

/** F2 — which source populates a facet's home app list. Favorites is manually curated; Recents/Most Used are `UsageStatsManager`-backed. */
enum class ListContentMode(@param:StringRes val displayNameRes: Int) {
    FAVORITES(R.string.list_content_mode_favorites),
    RECENTS(R.string.list_content_mode_recents),
    MOST_USED(R.string.list_content_mode_most_used),
}
