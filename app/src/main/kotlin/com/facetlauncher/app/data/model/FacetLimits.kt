package com.facetlauncher.app.data.model

/**
 * How many facets a user may have and which of them are usable. Free: 3. Pro: up to 10. A user who
 * loses Pro (a refund) keeps every facet, but only the first [FREE_MAX_FACETS] in list order stay
 * selectable until they delete down to that many or buy Pro again; nothing is deleted for them.
 */
object FacetLimits {
    const val MIN_FACETS = 1
    const val FREE_MAX_FACETS = 3
    const val PRO_MAX_FACETS = 10

    fun maxFor(isPro: Boolean): Int = if (isPro) PRO_MAX_FACETS else FREE_MAX_FACETS

    fun canAdd(facetCount: Int, isPro: Boolean): Boolean = facetCount < maxFor(isPro)

    /** [facetIdsInOrder] is the list order. Pro: all of them. Free: the first [FREE_MAX_FACETS]. */
    fun selectableIds(facetIdsInOrder: List<Long>, isPro: Boolean): Set<Long> =
        if (isPro) facetIdsInOrder.toSet() else facetIdsInOrder.take(FREE_MAX_FACETS).toSet()
}
