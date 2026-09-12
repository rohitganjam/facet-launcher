package com.facetlauncher.app.domain

import javax.inject.Inject

/**
 * Shared search-relevance ranking for every search surface with an app/contact-name match (the
 * Drawer's app search, its contacts section): items whose [key] *starts with* [query] rank first,
 * then everything else that merely *contains* [query] somewhere — both tiers kept alphabetical by
 * [key] internally, rather than search results reading in whatever incidental order the source
 * list/query happened to return (see chat history).
 */
class RankBySearchRelevanceUseCase @Inject constructor() {
    operator fun <T> invoke(items: List<T>, query: String, key: (T) -> String): List<T> {
        if (query.isBlank()) return items
        return items
            .filter { key(it).contains(query, ignoreCase = true) }
            .sortedWith(
                compareByDescending<T> { key(it).startsWith(query, ignoreCase = true) }
                    .thenBy { key(it).lowercase() },
            )
    }
}
