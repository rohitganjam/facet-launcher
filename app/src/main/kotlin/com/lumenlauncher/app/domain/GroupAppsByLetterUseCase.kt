package com.lumenlauncher.app.domain

import android.icu.text.AlphabeticIndex
import com.lumenlauncher.app.data.model.AppInfo
import java.util.Locale
import javax.inject.Inject

/**
 * The result of grouping an app list by leading letter, shared by both the App Drawer's List
 * and Grid presentations (and their alphabet rail).
 *
 * List renders a header per letter, so it scrolls by [headerIndexForLetter] — a flat "1 header
 * item + N per-app items" sequence. Grid renders no headers at all (a continuous grid, not
 * grouped visually) — see [DrawerGridContent][com.lumenlauncher.app.ui.drawer.AppDrawerScreen] —
 * so it scrolls by [firstAppIndexForLetter] instead: the index of that letter's first app within
 * the flat, header-less, already-alphabetical app sequence ([groups]' values concatenated in
 * order). Either way, [letters] and the rail's own drag/band-clamping logic are identical.
 */
data class GroupedApps(
    val groups: Map<String, List<AppInfo>>,
    val letters: List<String>,
    val headerIndexForLetter: Map<String, Int>,
    val firstAppIndexForLetter: Map<String, Int>,
)

/**
 * Groups apps by leading letter using [AlphabeticIndex] (F7) — the same ICU component the
 * system Contacts app uses — rather than naive `label.first().uppercase()` bucketing, so
 * locale-specific letters (Swedish Å/Ä/Ö, Spanish Ñ) bucket as their own distinct letters
 * instead of collapsing into "A"/"N", and non-Latin scripts transliterate into a Latin bucket
 * instead of all landing under a generic "#".
 */
class GroupAppsByLetterUseCase @Inject constructor() {

    operator fun invoke(apps: List<AppInfo>, locale: Locale = Locale.getDefault()): GroupedApps {
        // AlphabeticIndex's mutable form only supports iteration; getBucketIndex/getBucket (the
        // lookup-by-name API this needs) live on the ImmutableIndex it builds.
        val index = AlphabeticIndex<Unit>(locale).buildImmutableIndex()
        val groups = linkedMapOf<String, MutableList<AppInfo>>()
        for (app in apps) {
            val bucketLabel = index.getBucket(index.getBucketIndex(app.label)).label.ifBlank { "#" }
            groups.getOrPut(bucketLabel) { mutableListOf() }.add(app)
        }

        val letters = groups.keys.toList()
        val headerIndexForLetter = mutableMapOf<String, Int>()
        val firstAppIndexForLetter = mutableMapOf<String, Int>()
        var headerRunningIndex = 0
        var flatRunningIndex = 0
        for ((letter, appsInGroup) in groups) {
            headerIndexForLetter[letter] = headerRunningIndex
            headerRunningIndex += 1 + appsInGroup.size
            firstAppIndexForLetter[letter] = flatRunningIndex
            flatRunningIndex += appsInGroup.size
        }

        return GroupedApps(
            groups = groups,
            letters = letters,
            headerIndexForLetter = headerIndexForLetter,
            firstAppIndexForLetter = firstAppIndexForLetter,
        )
    }
}
