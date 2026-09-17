package com.facetlauncher.app.domain

import android.icu.text.AlphabeticIndex
import com.facetlauncher.app.data.model.AppInfo
import java.util.Locale
import javax.inject.Inject

/**
 * The result of grouping a list by each item's leading letter, shared by both the App Drawer's
 * List and Grid presentations (and their alphabet rail). Generic over [T] so the same bucketing
 * serves both the original apps-only grouping ([GroupedApps]) and the App Drawer's folders-shown-
 * inline mode, which groups a mixed `AppInfo`/`Folder` sequence
 * ([com.facetlauncher.app.data.model.DrawerItem]) by display name instead.
 *
 * List renders a header per letter, so it scrolls by [headerIndexForLetter] — a flat "1 header
 * item + N per-item items" sequence. Grid renders no headers at all (a continuous grid, not
 * grouped visually) — see [DrawerGridContent][com.facetlauncher.app.ui.drawer.AppDrawerScreen] —
 * so it scrolls by [firstAppIndexForLetter] instead: the index of that letter's first item within
 * the flat, header-less, already-alphabetical sequence ([groups]' values concatenated in order).
 * Either way, [letters] and the rail's own drag/band-clamping logic are identical.
 */
data class GroupedItems<T>(
    val groups: Map<String, List<T>>,
    val letters: List<String>,
    val headerIndexForLetter: Map<String, Int>,
    val firstAppIndexForLetter: Map<String, Int>,
)

/** The App Drawer's original apps-only grouping — kept as the concrete type most callers (and every existing test) already name. */
typealias GroupedApps = GroupedItems<AppInfo>

/**
 * Groups apps (or, via the generic overload, any list keyed by a display name) by leading letter
 * using [AlphabeticIndex] (F7) — the same ICU component the system Contacts app uses — rather
 * than naive `label.first().uppercase()` bucketing, so locale-specific letters (Swedish Å/Ä/Ö,
 * Spanish Ñ) bucket as their own distinct letters instead of collapsing into "A"/"N", and
 * non-Latin scripts transliterate into a Latin bucket instead of all landing under a generic "#".
 */
class GroupAppsByLetterUseCase @Inject constructor() {

    operator fun invoke(apps: List<AppInfo>, locale: Locale = Locale.getDefault()): GroupedApps =
        invoke(apps, locale) { it.label }

    /** Generic form behind [invoke] — used directly by the App Drawer's INLINE folder display mode to bucket [com.facetlauncher.app.data.model.DrawerItem] (apps and folders together) by [nameOf]. */
    operator fun <T> invoke(items: List<T>, locale: Locale = Locale.getDefault(), nameOf: (T) -> String): GroupedItems<T> {
        // AlphabeticIndex's mutable form only supports iteration; getBucketIndex/getBucket (the
        // lookup-by-name API this needs) live on the ImmutableIndex it builds.
        val index = AlphabeticIndex<Unit>(locale).buildImmutableIndex()
        val groups = linkedMapOf<String, MutableList<T>>()
        for (item in items) {
            val bucketLabel = index.getBucket(index.getBucketIndex(nameOf(item))).label.ifBlank { "#" }
            groups.getOrPut(bucketLabel) { mutableListOf() }.add(item)
        }

        val letters = groups.keys.toList()
        val headerIndexForLetter = mutableMapOf<String, Int>()
        val firstAppIndexForLetter = mutableMapOf<String, Int>()
        var headerRunningIndex = 0
        var flatRunningIndex = 0
        for ((letter, itemsInGroup) in groups) {
            headerIndexForLetter[letter] = headerRunningIndex
            headerRunningIndex += 1 + itemsInGroup.size
            firstAppIndexForLetter[letter] = flatRunningIndex
            flatRunningIndex += itemsInGroup.size
        }

        return GroupedItems(
            groups = groups,
            letters = letters,
            headerIndexForLetter = headerIndexForLetter,
            firstAppIndexForLetter = firstAppIndexForLetter,
        )
    }
}
