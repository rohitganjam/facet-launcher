package com.facetlauncher.app.data.model

/** One facet's Dock or Favorites list: every placement read and write names its list with one of these. */
data class PlacementTarget(val list: PlacementList, val facetId: Long) {
    /** The stored `ownerKey` for this list (see `AppPlacementEntity`). */
    val ownerKey: Long get() = facetId
}
