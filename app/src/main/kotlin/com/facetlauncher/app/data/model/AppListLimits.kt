package com.facetlauncher.app.data.model

/**
 * Single source of truth for Home's app-list size limits — previously duplicated (same value,
 * same meaning) across the placement repositories, the Recents/Most-used "Apps to show"
 * range, and every default value that seeds it (settings, facets, onboarding) — see chat
 * history. Anything bounding or defaulting one of these should reference this object rather than
 * repeating the number.
 */
object AppListLimits {
    /** Cap shared by both the launcher-wide default favorites list and each facet's own. */
    const val MAX_FAVORITES = 12

    /** The dock can be emptied out entirely (Home then omits the dock row) and holds at most [MAX_DOCK_APPS] items, apps and folders together. */
    const val MIN_DOCK_APPS = 0
    const val MAX_DOCK_APPS = 5

    /** [ListContentMode.RECENTS]/[ListContentMode.MOST_USED]'s "Apps to show" range and default. */
    const val MIN_APPS_TO_SHOW = 3
    const val MAX_APPS_TO_SHOW = 12
    const val DEFAULT_APPS_TO_SHOW = 6
    val APPS_TO_SHOW_OPTIONS = (MIN_APPS_TO_SHOW..MAX_APPS_TO_SHOW).toList()
}
