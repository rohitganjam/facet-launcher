package com.facetlauncher.app.data.model

/**
 * Single source of truth for Home's app-list size limits — previously duplicated (same value,
 * same meaning) across [com.facetlauncher.app.data.FavoriteAppRepository],
 * [com.facetlauncher.app.data.DefaultFavoriteAppRepository], the Recents/Most-used "Apps to show"
 * range, and every default value that seeds it (settings, profiles, onboarding) — see chat
 * history. Anything bounding or defaulting one of these should reference this object rather than
 * repeating the number.
 */
object AppListLimits {
    /** Cap shared by both the launcher-wide default favorites list and each profile's own. */
    const val MAX_FAVORITES = 6

    /** [ListContentMode.RECENTS]/[ListContentMode.MOST_USED]'s "Apps to show" range and default. */
    const val MIN_APPS_TO_SHOW = 3
    const val MAX_APPS_TO_SHOW = 6
    const val DEFAULT_APPS_TO_SHOW = 6
    val APPS_TO_SHOW_OPTIONS = (MIN_APPS_TO_SHOW..MAX_APPS_TO_SHOW).toList()
}
